package ru.playexstezi.onlinemod.network;

import ru.playexstezi.onlinemod.OnlineMod;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Optional, user-triggered UPnP IGD mapping for the Minecraft TCP listener.
 *
 * The mapping is restricted to the current game port and local host address.
 * It uses a one-hour lease, renews while the session is active, and attempts
 * to delete the mapping when hosting stops. It cannot bypass CGNAT.
 */
public final class UpnpPortMapper {
    private static final String SSDP_ADDRESS = "239.255.255.250";
    private static final int SSDP_PORT = 1900;
    private static final int LEASE_SECONDS = 3600;
    private static final int HTTP_TIMEOUT_MS = 3500;
    private static final int MAX_XML_BYTES = 65536;

    private static final ExecutorService IO = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "OnlineMod-UPnP");
        thread.setDaemon(true);
        return thread;
    });
    private static final ScheduledExecutorService RENEWER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "OnlineMod-UPnP-renew");
        thread.setDaemon(true);
        return thread;
    });

    private static volatile Mapping activeMapping;
    private static volatile ScheduledFuture<?> renewalTask;

    private UpnpPortMapper() {}

    public record MapResult(boolean success, String message) {}

    private record Service(String serviceType, URL controlUrl) {}
    private record Mapping(String serviceType, URL controlUrl, String localIp, int port) {}

    public static boolean isActive() {
        return activeMapping != null;
    }

    /** Starts UPnP discovery and mapping only after the user explicitly asks. */
    public static CompletableFuture<MapResult> mapAsync(String localIp, int port) {
        return CompletableFuture.supplyAsync(() -> mapNow(localIp, port), IO);
    }

    /** Deletes the current mapping, if one was created by this mod. */
    public static CompletableFuture<Boolean> closeAsync() {
        return CompletableFuture.supplyAsync(UpnpPortMapper::closeNow, IO);
    }

    private static synchronized MapResult mapNow(String localIp, int port) {
        if (port < 1 || port > 65535 || !isIpv4Literal(localIp)) {
            return new MapResult(false, "Invalid local address or server port.");
        }
        try {
            InetAddress localAddress = InetAddress.getByName(localIp);
            if (!localAddress.isSiteLocalAddress()) {
                return new MapResult(false, "The selected local address is not a private LAN IPv4 address.");
            }
        } catch (Exception exception) {
            return new MapResult(false, "The selected local address is invalid.");
        }

        Mapping current = activeMapping;
        if (current != null && current.localIp().equals(localIp) && current.port() == port) {
            return new MapResult(true, "UPnP port mapping is already active.");
        }
        if (current != null) closeMapping(current);
        cancelRenewal();

        try {
            Service service = discoverService(localIp);
            if (service == null) {
                return new MapResult(false, "No compatible UPnP router was found on this local network.");
            }
            Mapping mapping = new Mapping(service.serviceType(), service.controlUrl(), localIp, port);
            soap(mapping, "AddPortMapping", addMappingBody(mapping));
            activeMapping = mapping;
            scheduleRenewal();
            return new MapResult(true, "Router accepted the TCP port mapping.");
        } catch (Exception exception) {
            OnlineMod.LOGGER.warn("UPnP mapping could not be created: {}", exception.toString());
            return new MapResult(false, safeMessage(exception));
        }
    }

    private static synchronized boolean closeNow() {
        Mapping mapping = activeMapping;
        activeMapping = null;
        cancelRenewal();
        if (mapping == null) return false;
        try {
            closeMapping(mapping);
            return true;
        } catch (Exception exception) {
            OnlineMod.LOGGER.warn("Could not remove UPnP mapping: {}", exception.toString());
            return false;
        }
    }

    private static void closeMapping(Mapping mapping) {
        try {
            soap(mapping, "DeletePortMapping",
                    "<NewRemoteHost></NewRemoteHost>"
                    + "<NewExternalPort>" + mapping.port() + "</NewExternalPort>"
                    + "<NewProtocol>TCP</NewProtocol>");
        } catch (Exception exception) {
            OnlineMod.LOGGER.warn("UPnP mapping cleanup failed: {}", exception.toString());
        }
    }

    private static void scheduleRenewal() {
        cancelRenewal();
        renewalTask = RENEWER.scheduleAtFixedRate(() -> {
            Mapping mapping = activeMapping;
            if (mapping == null) return;
            try {
                soap(mapping, "AddPortMapping", addMappingBody(mapping));
            } catch (Exception exception) {
                OnlineMod.LOGGER.warn("UPnP mapping renewal failed: {}", exception.toString());
            }
        }, 30, 30, TimeUnit.MINUTES);
    }

    private static void cancelRenewal() {
        ScheduledFuture<?> task = renewalTask;
        renewalTask = null;
        if (task != null) task.cancel(false);
    }

    private static String addMappingBody(Mapping mapping) {
        return "<NewRemoteHost></NewRemoteHost>"
                + "<NewExternalPort>" + mapping.port() + "</NewExternalPort>"
                + "<NewProtocol>TCP</NewProtocol>"
                + "<NewInternalPort>" + mapping.port() + "</NewInternalPort>"
                + "<NewInternalClient>" + mapping.localIp() + "</NewInternalClient>"
                + "<NewEnabled>1</NewEnabled>"
                + "<NewPortMappingDescription>Online Mod Minecraft host</NewPortMappingDescription>"
                + "<NewLeaseDuration>" + LEASE_SECONDS + "</NewLeaseDuration>";
    }

    private static Service discoverService(String localIp) throws Exception {
        Set<URL> descriptions = discoverDescriptions(localIp);
        for (URL descriptionUrl : descriptions) {
            try {
                Document document = fetchXml(descriptionUrl);
                NodeList services = document.getElementsByTagNameNS("*", "service");
                for (int i = 0; i < services.getLength(); i++) {
                    if (!(services.item(i) instanceof Element element)) continue;
                    String type = childText(element, "serviceType");
                    String control = childText(element, "controlURL");
                    if (type == null || control == null) continue;
                    if (!(type.startsWith("urn:schemas-upnp-org:service:WANIPConnection:")
                            || type.startsWith("urn:schemas-upnp-org:service:WANPPPConnection:"))) {
                        continue;
                    }
                    URL controlUrl = new URL(descriptionUrl, control);
                    validateRouterUrl(controlUrl, descriptionUrl.getHost());
                    return new Service(type, controlUrl);
                }
            } catch (Exception exception) {
                OnlineMod.LOGGER.debug("Ignoring an unusable UPnP device description: {}", exception.toString());
            }
        }
        return null;
    }

    private static Set<URL> discoverDescriptions(String localIp) throws Exception {
        Set<URL> locations = new LinkedHashSet<>();
        byte[] request = ("M-SEARCH * HTTP/1.1\r\n"
                + "HOST: " + SSDP_ADDRESS + ":" + SSDP_PORT + "\r\n"
                + "MAN: \"ssdp:discover\"\r\n"
                + "MX: 2\r\n"
                + "ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1\r\n"
                + "\r\n").getBytes(StandardCharsets.US_ASCII);

        InetAddress local = InetAddress.getByName(localIp);
        InetAddress multicast = InetAddress.getByName(SSDP_ADDRESS);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(local, 0))) {
            socket.setSoTimeout(350);
            socket.send(new DatagramPacket(request, request.length,
                    new InetSocketAddress(multicast, SSDP_PORT)));
            byte[] buffer = new byte[8192];
            while (System.nanoTime() < deadline && locations.size() < 8) {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(response);
                } catch (java.net.SocketTimeoutException timeout) {
                    continue;
                }
                String headers = new String(response.getData(), response.getOffset(),
                        response.getLength(), StandardCharsets.ISO_8859_1);
                for (String line : headers.split("\\r?\\n")) {
                    if (line.regionMatches(true, 0, "LOCATION:", 0, 9)) {
                        String value = line.substring(9).trim();
                        try {
                            URL url = URI.create(value).toURL();
                            validateRouterUrl(url, null);
                            locations.add(url);
                        } catch (Exception ignored) {
                            // Ignore malformed or non-local device URLs.
                        }
                    }
                }
            }
        }
        return locations;
    }

    private static void validateRouterUrl(URL url, String expectedHost) throws Exception {
        if (!"http".equalsIgnoreCase(url.getProtocol())
                || url.getUserInfo() != null
                || url.getHost() == null
                || url.getHost().isBlank()) {
            throw new IllegalArgumentException("Router returned an unsupported UPnP URL.");
        }
        if (expectedHost != null && !url.getHost().equalsIgnoreCase(expectedHost)) {
            throw new IllegalArgumentException("UPnP control URL points to a different host.");
        }
        InetAddress[] addresses = InetAddress.getAllByName(url.getHost());
        if (addresses.length == 0) throw new IllegalArgumentException("UPnP router address could not be resolved.");
        for (InetAddress address : addresses) {
            if (!(address.isSiteLocalAddress() || address.isLinkLocalAddress())) {
                throw new IllegalArgumentException("UPnP URL does not point to a local router.");
            }
        }
    }

    private static Document fetchXml(URL url) throws Exception {
        validateRouterUrl(url, null);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(HTTP_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        try {
            int status = connection.getResponseCode();
            if (status != 200) throw new IllegalStateException("Router description request failed (HTTP " + status + ").");
            byte[] bytes = readLimited(connection.getInputStream());
            DocumentBuilderFactory factory = secureXmlFactory();
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
        } finally {
            connection.disconnect();
        }
    }

    private static void soap(Mapping mapping, String action, String body) throws Exception {
        String serviceType = mapping.serviceType();
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body><u:" + action + " xmlns:u=\"" + serviceType + "\">"
                + body + "</u:" + action + "></s:Body></s:Envelope>";
        HttpURLConnection connection = (HttpURLConnection) mapping.controlUrl().openConnection();
        connection.setConnectTimeout(HTTP_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
        connection.setRequestProperty("SOAPAction", "\"" + serviceType + "#" + action + "\"");
        byte[] payload = xml.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(payload.length);
        try {
            connection.getOutputStream().write(payload);
            int status = connection.getResponseCode();
            if (status != 200) {
                String detail = "";
                InputStream error = connection.getErrorStream();
                if (error != null) detail = new String(readLimited(error), StandardCharsets.UTF_8);
                throw new IllegalStateException("Router rejected " + action + " (HTTP " + status + ")."
                        + (detail.isBlank() ? "" : " " + extractUpnpError(detail)));
            }
            InputStream response = connection.getInputStream();
            if (response != null) readLimited(response);
        } finally {
            connection.disconnect();
        }
    }

    private static String extractUpnpError(String xml) {
        try {
            Document document = secureXmlFactory().newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            String code = childText(document.getDocumentElement(), "errorCode");
            String description = childText(document.getDocumentElement(), "errorDescription");
            if (code != null) return "UPnP error " + code + (description == null ? "" : ": " + description);
        } catch (Exception ignored) {
            // Router error bodies are not always valid SOAP XML.
        }
        return "";
    }

    private static DocumentBuilderFactory secureXmlFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory;
    }

    private static String childText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) return null;
        String value = nodes.item(0).getTextContent();
        return value == null ? null : value.trim();
    }

    private static byte[] readLimited(InputStream input) throws Exception {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = stream.read(buffer)) != -1) {
                total += read;
                if (total > MAX_XML_BYTES) throw new IllegalStateException("Router response is too large.");
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static boolean isIpv4Literal(String value) {
        if (value == null || !value.matches("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")) return false;
        String[] parts = value.split("\\.");
        for (String part : parts) {
            try {
                int octet = Integer.parseInt(part);
                if (octet < 0 || octet > 255) return false;
            } catch (NumberFormatException exception) {
                return false;
            }
        }
        return true;
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) return "UPnP setup failed. Check router support and local network settings.";
        if (message.length() > 180) return message.substring(0, 180);
        return message;
    }
}

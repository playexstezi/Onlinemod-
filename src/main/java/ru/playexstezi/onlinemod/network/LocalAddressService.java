package ru.playexstezi.onlinemod.network;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;

/** Finds a private IPv4 address usable by devices on the local network. */
public final class LocalAddressService {
    private LocalAddressService() {}

    public static Optional<String> discoverIpv4() {
        // UDP connect selects the OS route but does not send application data.
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("1.1.1.1"), 53);
            InetAddress selected = socket.getLocalAddress();
            if (isPrivateIpv4(selected)) return Optional.of(selected.getHostAddress());
        } catch (Exception ignored) {
            // Fall back to active interface enumeration.
        }

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) continue;
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (isPrivateIpv4(address)) return Optional.of(address.getHostAddress());
                }
            }
        } catch (SocketException ignored) {
            // No active network interface was available.
        }
        return Optional.empty();
    }

    private static boolean isPrivateIpv4(InetAddress address) {
        return address instanceof Inet4Address
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isLinkLocalAddress()
                && address.isSiteLocalAddress();
    }
}

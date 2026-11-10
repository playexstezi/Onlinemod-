package ru.playexstezi.onlinemod.network;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * A direct connection endpoint in host:port form.
 * IPv6 literals must be written in [address]:port form.
 */
public record ConnectionAddress(String host, int port) {
    public ConnectionAddress {
        if (!isValidHost(host)) {
            throw new IllegalArgumentException("Host must be a valid hostname, IPv4 address, or IPv6 literal");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    public static Optional<ConnectionAddress> parse(String input) {
        if (input == null) {
            return Optional.empty();
        }

        String value = input.trim();
        if (value.isEmpty()) {
            return Optional.empty();
        }

        String host;
        String portText;

        if (value.startsWith("[")) {
            int closingBracket = value.indexOf(']');
            if (closingBracket < 2 || closingBracket + 1 >= value.length()
                    || value.charAt(closingBracket + 1) != ':') {
                return Optional.empty();
            }
            host = value.substring(1, closingBracket);
            portText = value.substring(closingBracket + 2);
            if (!host.contains(":") || !isValidIpv6(host)) {
                return Optional.empty();
            }
        } else {
            int separator = value.lastIndexOf(':');
            if (separator <= 0 || separator != value.indexOf(':')) {
                return Optional.empty();
            }
            host = value.substring(0, separator);
            portText = value.substring(separator + 1);
        }

        if (portText.isEmpty() || !portText.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return Optional.empty();
        }

        try {
            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                return Optional.empty();
            }
            return Optional.of(new ConnectionAddress(host, port));
        } catch (NumberFormatException | IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static boolean isValidHost(String host) {
        if (host == null || host.isEmpty() || !host.equals(host.trim())) {
            return false;
        }
        if (host.indexOf(':') >= 0) {
            return isValidIpv6(host);
        }
        if (host.matches("[0-9.]+")) {
            return isValidIpv4(host);
        }
        if (host.length() > 253) {
            return false;
        }

        String name = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        if (name.isEmpty()) {
            return false;
        }
        for (String label : name.split("\\.", -1)) {
            if (label.isEmpty() || label.length() > 63
                    || label.charAt(0) == '-' || label.charAt(label.length() - 1) == '-') {
                return false;
            }
            for (int i = 0; i < label.length(); i++) {
                char c = label.charAt(i);
                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                        || (c >= '0' && c <= '9') || c == '-')) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidIpv4(String host) {
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3
                    || !part.chars().allMatch(c -> c >= '0' && c <= '9')) {
                return false;
            }
            try {
                if (Integer.parseInt(part) > 255) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv6(String host) {
        if (host == null || host.isEmpty() || host.indexOf('%') >= 0
                || !host.matches("[0-9a-fA-F:.]+")) {
            return false;
        }
        try {
            return InetAddress.getByName(host) instanceof Inet6Address;
        } catch (UnknownHostException ignored) {
            return false;
        }
    }

    public String format() {
        String formattedHost = host.contains(":") ? "[" + host + "]" : host;
        return formattedHost + ":" + port;
    }
}

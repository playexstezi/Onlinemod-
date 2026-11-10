package ru.playexstezi.onlinemod.network;

import java.util.Optional;

/**
 * A direct connection endpoint in host:port form.
 * IPv6 literals must be written in [address]:port form.
 */
public record ConnectionAddress(String host, int port) {
    public ConnectionAddress {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host cannot be empty");
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
            if (!host.contains(":")) {
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

        if (host.isBlank() || portText.isBlank()
                || !portText.chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }

        try {
            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                return Optional.empty();
            }
            return Optional.of(new ConnectionAddress(host, port));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public String format() {
        String formattedHost = host.contains(":") ? "[" + host + "]" : host;
        return formattedHost + ":" + port;
    }
}

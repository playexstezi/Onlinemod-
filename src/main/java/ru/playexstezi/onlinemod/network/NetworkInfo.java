package ru.playexstezi.onlinemod.network;

import java.util.Optional;

/**
 * Immutable snapshot shown by the future host screen.
 */
public record NetworkInfo(
        Optional<String> publicIpv4,
        int gamePort,
        NetworkStatus status,
        String explanation
) {
    public NetworkInfo {
        publicIpv4 = publicIpv4 == null ? Optional.empty() : publicIpv4;
        if (gamePort < 1 || gamePort > 65535) {
            throw new IllegalArgumentException("Invalid game port");
        }
        if (status == null) {
            status = NetworkStatus.NOT_CHECKED;
        }
        explanation = explanation == null ? "" : explanation;
    }

    public Optional<ConnectionAddress> directAddress() {
        return publicIpv4.map(ip -> new ConnectionAddress(ip, gamePort));
    }
}

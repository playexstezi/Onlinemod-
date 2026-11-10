package ru.playexstezi.onlinemod.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Discovers the public IPv4 address visible to the internet.
 *
 * This does not open a port or guarantee that the host is reachable. Router,
 * firewall, ISP and CGNAT restrictions are checked separately.
 */
public final class PublicIpService {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final List<URI> PROVIDERS = List.of(
            URI.create("https://api.ipify.org"),
            URI.create("https://checkip.amazonaws.com")
    );

    private PublicIpService() {
    }

    public static CompletableFuture<Optional<String>> discoverIpv4() {
        return tryProvider(0);
    }

    private static CompletableFuture<Optional<String>> tryProvider(int index) {
        if (index >= PROVIDERS.size()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        HttpRequest request = HttpRequest.newBuilder(PROVIDERS.get(index))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "text/plain")
                .GET()
                .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        Optional<String> address = validateIpv4(response.body().trim());
                        if (address.isPresent()) {
                            return CompletableFuture.completedFuture(address);
                        }
                    }
                    return tryProvider(index + 1);
                })
                .exceptionallyCompose(error -> tryProvider(index + 1));
    }

    private static Optional<String> validateIpv4(String value) {
        String[] parts = value.split("\\.", -1);
        if (parts.length != 4) {
            return Optional.empty();
        }

        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty() || !parts[i].chars().allMatch(Character::isDigit)) {
                return Optional.empty();
            }
            try {
                int octet = Integer.parseInt(parts[i]);
                if (octet < 0 || octet > 255) {
                    return Optional.empty();
                }
                if (i > 0) {
                    normalized.append('.');
                }
                normalized.append(octet);
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.of(normalized.toString());
    }
}

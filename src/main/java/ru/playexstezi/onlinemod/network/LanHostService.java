package ru.playexstezi.onlinemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;

import java.util.Optional;

/**
 * Controls the vanilla integrated-server LAN publication.
 *
 * Opening LAN only creates a listening socket on the host computer. It does
 * not configure the router, bypass CGNAT, or prove that the public address is
 * reachable from the internet.
 */
public final class LanHostService {
    private static final int RANDOM_PORT = 0;

    private LanHostService() {
    }

    /**
     * Opens the current single-player world to network connections.
     *
     * @param allowCommands whether joining players receive operator commands
     * @return the actual bound port when publication succeeds
     */
    public static Optional<Integer> openCurrentWorld(boolean allowCommands) {
        Minecraft minecraft = Minecraft.getInstance();
        IntegratedServer server = minecraft.getSingleplayerServer();

        if (server == null || !server.isRunning()) {
            return Optional.empty();
        }

        if (server.isPublished()) {
            int currentPort = server.getPort();
            return currentPort > 0 ? Optional.of(currentPort) : Optional.empty();
        }

        boolean opened = server.publishServer(
                server.getDefaultGameType(),
                allowCommands,
                RANDOM_PORT
        );

        if (!opened) {
            return Optional.empty();
        }

        int actualPort = server.getPort();
        return actualPort > 0 ? Optional.of(actualPort) : Optional.empty();
    }

    /**
     * Returns the currently published port, if the integrated server is open.
     */
    public static Optional<Integer> getPublishedPort() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.isRunning() || !server.isPublished()) {
            return Optional.empty();
        }

        int port = server.getPort();
        return port > 0 ? Optional.of(port) : Optional.empty();
    }

    /**
     * Stops accepting incoming TCP connections for this integrated server.
     * The integrated world itself remains running and is not shut down.
     *
     * Minecraft's 1.20.1 public API does not expose a dedicated unpublish-LAN
     * method. Stopping the connection listener closes its published network
     * sockets and disconnects remote players. Restarting publication in the
     * same world is intentionally not supported by this first implementation.
     */
    public static boolean closeIncomingConnections() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.isRunning() || !server.isPublished()) {
            return false;
        }

        if (server.getConnection() == null) {
            return false;
        }

        server.getConnection().stop();
        return true;
    }
}

package ru.playexstezi.onlinemod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import ru.playexstezi.onlinemod.network.LanHostService;
import ru.playexstezi.onlinemod.network.PublicIpService;

import java.util.Optional;

/**
 * First host-management screen. It intentionally labels the published port
 * as LAN-only until router mapping and external reachability are implemented.
 */
public final class HostScreen extends Screen {
    private final Screen parent;
    private String publicIp = "Checking...";
    private String status = "No online session started.";
    private int publishedPort = -1;

    public HostScreen(Screen parent) {
        super(Component.literal("Online Mod"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int buttonWidth = 200;
        int buttonX = center - buttonWidth / 2;
        int top = this.height / 2 - 36;

        this.addRenderableWidget(Button.builder(
                Component.literal("Start hosting"),
                button -> startHosting()
        ).bounds(buttonX, top, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Stop hosting"),
                button -> stopHosting()
        ).bounds(buttonX, top + 26, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Refresh public IP"),
                button -> refreshPublicIp()
        ).bounds(buttonX, top + 52, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                button -> this.minecraft.setScreen(parent)
        ).bounds(buttonX, top + 84, buttonWidth, 20).build());

        refreshPublicIp();
        refreshSessionState();
    }

    private void refreshPublicIp() {
        this.publicIp = "Checking...";
        PublicIpService.discoverIpv4().thenAccept(result ->
                Minecraft.getInstance().execute(() ->
                        this.publicIp = result.orElse("Unavailable")
                )
        );
    }

    private void refreshSessionState() {
        Optional<Integer> port = LanHostService.getPublishedPort();
        this.publishedPort = port.orElse(-1);
        if (port.isPresent()) {
            this.status = "Incoming connections are open on the LAN listener.";
        }
    }

    private void startHosting() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.isRunning()) {
            this.status = "Open a single-player world first.";
            return;
        }

        this.status = "Starting LAN listener...";
        server.execute(() -> {
            Optional<Integer> port = LanHostService.openCurrentWorld(false);
            Minecraft.getInstance().execute(() -> {
                this.publishedPort = port.orElse(-1);
                this.status = port.isPresent()
                        ? "LAN listener started. Router access is not configured."
                        : "Could not start hosting. Restart the world if it was stopped.";
            });
        });
    }

    private void stopHosting() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.isRunning()) {
            this.status = "No single-player world is running.";
            return;
        }

        this.status = "Closing incoming connections...";
        server.execute(() -> {
            boolean stopped = LanHostService.closeIncomingConnections();
            Minecraft.getInstance().execute(() -> {
                if (stopped) {
                    this.publishedPort = -1;
                    this.status = "Incoming connections closed. The world is still running.";
                } else {
                    this.status = "No active host listener was found.";
                }
            });
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 38, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.literal("Public IPv4: " + this.publicIp),
                this.width / 2,
                this.height / 2 - 76,
                0xD0D0D0
        );

        String portText = this.publishedPort > 0
                ? "Local server port: " + this.publishedPort + " (LAN only)"
                : "Local server port: not open";
        graphics.drawCenteredString(
                this.font,
                Component.literal(portText),
                this.width / 2,
                this.height / 2 - 60,
                0xD0D0D0
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.status),
                this.width / 2,
                this.height / 2 + 76,
                0xFFFFFF
        );
        graphics.drawCenteredString(
                this.font,
                Component.literal("A public IP does not mean friends can connect yet."),
                this.width / 2,
                this.height / 2 + 90,
                0xAAAAAA
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

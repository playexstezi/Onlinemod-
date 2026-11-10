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

/** Host controls for the current integrated single-player world. */
public final class HostScreen extends Screen {
    private final Screen parent;
    private String publicIp = "onlinemod.status.checking";
    private String status = "onlinemod.status.idle";
    private int publishedPort = -1;

    public HostScreen(Screen parent) {
        super(Component.translatable("onlinemod.screen.host.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int buttonWidth = Math.min(220, this.width - 40);
        int buttonX = center - buttonWidth / 2;
        int top = this.height / 2 - 62;
        int step = 24;

        this.addRenderableWidget(Button.builder(
                Component.translatable("onlinemod.button.start_hosting"),
                button -> startHosting()
        ).bounds(buttonX, top, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("onlinemod.button.stop_hosting"),
                button -> stopHosting()
        ).bounds(buttonX, top + step, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("onlinemod.button.join_friend"),
                button -> Minecraft.getInstance().setScreen(new JoinScreen(this))
        ).bounds(buttonX, top + step * 2, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("onlinemod.button.refresh_ip"),
                button -> refreshPublicIp()
        ).bounds(buttonX, top + step * 3, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("onlinemod.button.copy_ip"),
                button -> copyPublicIp()
        ).bounds(buttonX, top + step * 4, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> Minecraft.getInstance().setScreen(parent)
        ).bounds(buttonX, top + step * 5 + 5, buttonWidth, 20).build());

        refreshPublicIp();
        refreshSessionState();
    }

    private void refreshPublicIp() {
        this.publicIp = "onlinemod.status.checking";
        PublicIpService.discoverIpv4().thenAccept(result ->
                Minecraft.getInstance().execute(() ->
                        this.publicIp = result.orElse("onlinemod.status.unavailable")
                )
        );
    }

    private void refreshSessionState() {
        Optional<Integer> port = LanHostService.getPublishedPort();
        this.publishedPort = port.orElse(-1);
        if (port.isPresent()) {
            this.status = "onlinemod.status.lan_open";
        }
    }

    private void copyPublicIp() {
        if ("onlinemod.status.checking".equals(this.publicIp)
                || "onlinemod.status.unavailable".equals(this.publicIp)) {
            this.status = "onlinemod.status.ip_not_ready";
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(this.publicIp);
        this.status = "onlinemod.status.ip_copied";
    }

    private void startHosting() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.isRunning()) {
            this.status = "onlinemod.status.open_world_first";
            return;
        }

        this.status = "onlinemod.status.starting";
        server.execute(() -> {
            Optional<Integer> port = LanHostService.openCurrentWorld(false);
            Minecraft.getInstance().execute(() -> {
                this.publishedPort = port.orElse(-1);
                this.status = port.isPresent()
                        ? "onlinemod.status.started_lan"
                        : "onlinemod.status.start_failed";
            });
        });
    }

    private void stopHosting() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.isRunning()) {
            this.status = "onlinemod.status.no_world";
            return;
        }

        this.status = "onlinemod.status.stopping";
        server.execute(() -> {
            boolean stopped = LanHostService.closeIncomingConnections();
            Minecraft.getInstance().execute(() -> {
                if (stopped) {
                    this.publishedPort = -1;
                    this.status = "onlinemod.status.stopped_world_kept";
                } else {
                    this.status = "onlinemod.status.no_listener";
                }
            });
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int center = this.width / 2;
        int panelWidth = Math.min(360, this.width - 20);
        int panelLeft = center - panelWidth / 2;
        int panelTop = this.height / 2 - 92;

        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 206, 0xB0101722);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 1, 0xFF4A9BD8);

        graphics.drawCenteredString(this.font, this.title, center, panelTop + 10, 0xFFFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("onlinemod.label.public_ip",
                        Component.translatable(this.publicIp)),
                center,
                panelTop + 29,
                0xFFE5EAF0
        );

        Component portText = this.publishedPort > 0
                ? Component.translatable("onlinemod.label.port_open", this.publishedPort)
                : Component.translatable("onlinemod.label.port_closed");
        graphics.drawCenteredString(this.font, portText, center, panelTop + 43, 0xFFD0D8E2);

        graphics.drawCenteredString(
                this.font,
                Component.translatable(this.status),
                center,
                panelTop + 184,
                0xFFFFD479
        );
        graphics.drawCenteredString(
                this.font,
                Component.translatable("onlinemod.label.internet_warning"),
                center,
                panelTop + 195,
                0xFFB8C3D0
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

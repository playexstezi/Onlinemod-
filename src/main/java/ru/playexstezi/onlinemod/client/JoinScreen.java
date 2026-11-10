package ru.playexstezi.onlinemod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import ru.playexstezi.onlinemod.network.ConnectionAddress;

/** Connects through Minecraft's normal multiplayer connection pipeline. */
public final class JoinScreen extends Screen {
    private final Screen parent;
    private EditBox addressInput;
    private String error = "";

    public JoinScreen(Screen parent) {
        super(Component.translatable("onlinemod.screen.join.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int buttonWidth = Math.min(220, this.width - 40);
        int buttonX = center - buttonWidth / 2;

        this.addressInput = new EditBox(
                this.font,
                buttonX,
                this.height / 2 - 20,
                buttonWidth,
                20,
                Component.translatable("onlinemod.label.server_address")
        );
        this.addressInput.setMaxLength(255);
        this.addressInput.setHint(Component.literal("example.com:25565"));
        this.addRenderableWidget(this.addressInput);

        this.addRenderableWidget(Button.builder(
                Component.translatable("onlinemod.button.connect"),
                button -> connect()
        ).bounds(buttonX, this.height / 2 + 12, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> Minecraft.getInstance().setScreen(parent)
        ).bounds(buttonX, this.height / 2 + 40, buttonWidth, 20).build());

        this.setInitialFocus(this.addressInput);
    }

    private void connect() {
        var parsed = ConnectionAddress.parse(this.addressInput.getValue());
        if (parsed.isEmpty()) {
            this.error = "onlinemod.error.invalid_address";
            return;
        }

        ConnectionAddress endpoint = parsed.get();
        ServerAddress serverAddress = new ServerAddress(endpoint.host(), endpoint.port());
        ConnectScreen.startConnecting(this, Minecraft.getInstance(), serverAddress, null, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("onlinemod.label.join_hint"),
                this.width / 2,
                this.height / 2 - 48,
                0xFFD0D8E2
        );

        if (!this.error.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(this.error),
                    this.width / 2,
                    this.height / 2 + 70,
                    0xFFFF7777
            );
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

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

/**
 * Connects to a friend using a host:port address through Minecraft's normal
 * multiplayer connection flow.
 */
public final class JoinScreen extends Screen {
    private final Screen parent;
    private EditBox addressInput;
    private String error = "";

    public JoinScreen(Screen parent) {
        super(Component.literal("Join Online Mod session"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int buttonWidth = 200;
        int buttonX = center - buttonWidth / 2;

        this.addressInput = new EditBox(
                this.font,
                buttonX,
                this.height / 2 - 20,
                buttonWidth,
                20,
                Component.literal("Server address")
        );
        this.addressInput.setMaxLength(255);
        this.addressInput.setHint(Component.literal("example.com:25565"));
        this.addRenderableWidget(this.addressInput);

        this.addRenderableWidget(Button.builder(
                Component.literal("Connect"),
                button -> connect()
        ).bounds(buttonX, this.height / 2 + 12, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Back"),
                button -> Minecraft.getInstance().setScreen(parent)
        ).bounds(buttonX, this.height / 2 + 40, buttonWidth, 20).build());

        this.setInitialFocus(this.addressInput);
    }

    private void connect() {
        String input = this.addressInput.getValue();
        var parsed = ConnectionAddress.parse(input);
        if (parsed.isEmpty()) {
            this.error = "Enter an address in host:port format.";
            return;
        }

        ConnectionAddress endpoint = parsed.get();
        ServerAddress serverAddress = new ServerAddress(endpoint.host(), endpoint.port());
        ConnectScreen.startConnecting(
                this,
                Minecraft.getInstance(),
                serverAddress,
                null,
                false
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.literal("Enter the address shared by the host (host:port)."),
                this.width / 2,
                this.height / 2 - 48,
                0xD0D0D0
        );

        if (!this.error.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal(this.error),
                    this.width / 2,
                    this.height / 2 + 70,
                    0xFF7777
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

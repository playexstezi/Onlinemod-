package ru.playexstezi.onlinemod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.playexstezi.onlinemod.OnlineMod;

/** Adds Online Mod shortcuts to vanilla menus. */
@Mod.EventBusSubscriber(modid = OnlineMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ScreenHooks {
    private ScreenHooks() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof PauseScreen) {
            event.addListener(Button.builder(
                    Component.translatable("onlinemod.button.menu_host"),
                    button -> Minecraft.getInstance().setScreen(new HostScreen(screen))
            ).bounds(screen.width / 2 - 100, screen.height - 28, 200, 20).build());
            return;
        }

        if (screen instanceof JoinMultiplayerScreen) {
            event.addListener(Button.builder(
                    Component.translatable("onlinemod.button.menu_join"),
                    button -> Minecraft.getInstance().setScreen(new JoinScreen(screen))
            ).bounds(screen.width / 2 - 100, screen.height - 52, 200, 20).build());
        }
    }
}

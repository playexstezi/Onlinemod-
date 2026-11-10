package ru.playexstezi.onlinemod.client;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.playexstezi.onlinemod.OnlineMod;
import ru.playexstezi.onlinemod.network.PublicIpService;

/**
 * Temporary diagnostic command while the in-game host screen is being built.
 * Usage: /onlinemod ip
 */
@Mod.EventBusSubscriber(modid = OnlineMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientCommands {
    private ClientCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("onlinemod")
                        .then(Commands.literal("ip")
                                .executes(context -> {
                                    var source = context.getSource();
                                    source.sendSuccess(
                                            () -> Component.literal("Online Mod: checking public IPv4 address..."),
                                            false
                                    );

                                    PublicIpService.discoverIpv4().thenAccept(result ->
                                            Minecraft.getInstance().execute(() -> {
                                                if (result.isPresent()) {
                                                    source.sendSuccess(
                                                            () -> Component.literal("Public IPv4: " + result.get()),
                                                            false
                                                    );
                                                    source.sendSuccess(
                                                            () -> Component.literal(
                                                                    "This is only your public address. It does not mean the Minecraft port is open."
                                                            ),
                                                            false
                                                    );
                                                } else {
                                                    source.sendFailure(Component.literal(
                                                            "Could not discover your public IPv4 address. Check your internet connection."
                                                    ));
                                                }
                                            })
                                    );
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }
}

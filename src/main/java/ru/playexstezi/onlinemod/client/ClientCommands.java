package ru.playexstezi.onlinemod.client;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.playexstezi.onlinemod.OnlineMod;
import ru.playexstezi.onlinemod.network.LanHostService;
import ru.playexstezi.onlinemod.network.PublicIpService;

/**
 * Temporary diagnostic and session commands while the in-game screens are
 * being built.
 *
 * Usage:
 *   /onlinemod ip
 *   /onlinemod host
 *   /onlinemod stop
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
                        .then(Commands.literal("host")
                                .executes(context -> {
                                    var source = context.getSource();
                                    IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
                                    if (server == null || !server.isRunning()) {
                                        source.sendFailure(Component.literal(
                                                "Open a single-player world before starting an Online Mod session."
                                        ));
                                        return 0;
                                    }

                                    server.execute(() -> {
                                        var port = LanHostService.openCurrentWorld(false);
                                        Minecraft.getInstance().execute(() -> {
                                            if (port.isPresent()) {
                                                source.sendSuccess(
                                                        () -> Component.literal(
                                                                "Incoming connections enabled on port " + port.get() + "."
                                                        ),
                                                        false
                                                );
                                                source.sendSuccess(
                                                        () -> Component.literal(
                                                                "This is currently LAN-only. Router and firewall access are not configured yet."
                                                        ),
                                                        false
                                                );
                                            } else {
                                                source.sendFailure(Component.literal(
                                                        "Could not open the integrated server for network connections."
                                                ));
                                            }
                                        });
                                    });
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("stop")
                                .executes(context -> {
                                    var source = context.getSource();
                                    IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
                                    if (server == null || !server.isRunning()) {
                                        source.sendFailure(Component.literal("No single-player world is running."));
                                        return 0;
                                    }

                                    server.execute(() -> {
                                        boolean stopped = LanHostService.closeIncomingConnections();
                                        Minecraft.getInstance().execute(() -> {
                                            if (stopped) {
                                                source.sendSuccess(
                                                        () -> Component.literal(
                                                                "Incoming network connections have been closed. Your world is still running."
                                                        ),
                                                        false
                                                );
                                                source.sendSuccess(
                                                        () -> Component.literal(
                                                                "To host again, close and reopen the world. Reopening in the same session is not supported yet."
                                                        ),
                                                        false
                                                );
                                            } else {
                                                source.sendFailure(Component.literal(
                                                        "No active Online Mod host session was found."
                                                ));
                                            }
                                        });
                                    });
                                    return Command.SINGLE_SUCCESS;
                                }))
        );
    }
}

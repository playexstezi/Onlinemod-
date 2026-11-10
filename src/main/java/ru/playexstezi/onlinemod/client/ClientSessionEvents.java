package ru.playexstezi.onlinemod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.playexstezi.onlinemod.OnlineMod;
import ru.playexstezi.onlinemod.network.UpnpPortMapper;

/** Removes this mod's temporary router mapping when the client leaves a world. */
@Mod.EventBusSubscriber(modid = OnlineMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientSessionEvents {
    private ClientSessionEvents() {}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        UpnpPortMapper.closeAsync();
    }
}

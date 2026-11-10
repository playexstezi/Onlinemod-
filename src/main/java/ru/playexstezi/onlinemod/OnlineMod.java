package ru.playexstezi.onlinemod;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Main entry point for Online Mod.
 *
 * Networking is intentionally kept out of the mod constructor. The first
 * milestone establishes a clean Forge 1.20.1 project before adding session
 * lifecycle, address discovery, and connection UI.
 */
@Mod(OnlineMod.MOD_ID)
public final class OnlineMod {
    public static final String MOD_ID = "onlinemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OnlineMod() {
        LOGGER.info("Online Mod initialized. Network session services are not enabled yet.");
    }
}

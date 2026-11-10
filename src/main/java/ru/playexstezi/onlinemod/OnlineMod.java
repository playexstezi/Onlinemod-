package ru.playexstezi.onlinemod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/** Main entry point for Online Mod. */
@Mod(OnlineMod.MOD_ID)
public final class OnlineMod {
    public static final String MOD_ID = "onlinemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OnlineMod() {
        LOGGER.info("Online Mod initialized.");
    }
}

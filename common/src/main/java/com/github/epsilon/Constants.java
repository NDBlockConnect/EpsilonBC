package com.github.epsilon;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    public static final Minecraft mc = Minecraft.getInstance();

    public static final String NAME = "EpsilonBC";

    public static final String MOD_ID = BuildConfig.MOD_ID;

    public static final String VERSION = BuildConfig.VERSION;

    // Human-facing version shown in the UI (e.g. "v26.0 Alpha 2"). VERSION stays valid SemVer for loaders.
    public static final String DISPLAY_VERSION = BuildConfig.DISPLAY_VERSION;

    public static final Logger LOGGER = LogManager.getLogger(Constants.NAME);

}

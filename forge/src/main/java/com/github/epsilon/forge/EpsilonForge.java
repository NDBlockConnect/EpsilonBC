package com.github.epsilon.forge;

import com.github.epsilon.Constants;
import com.github.epsilon.EpsilonClient;
import com.github.epsilon.addon.EpsilonAddonManager;
import com.github.epsilon.forge.addon.EpsilonAddonSetupEvent;
import com.github.epsilon.forge.addon.ForgeSelfAddonRegistrar;
import net.minecraftforge.common.MinecraftForge;

public class EpsilonForge {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        Constants.LOGGER.info("Initializing Epsilon on Forge...");

        // 1. Register built-in Forge addon
        ForgeSelfAddonRegistrar.register();

        // 2. Fire addon setup event to collect all addons
        EpsilonAddonSetupEvent setupEvent = new EpsilonAddonSetupEvent();
        MinecraftForge.EVENT_BUS.post(setupEvent);

        // 3. Register collected addons
        for (var addon : setupEvent.getAddons()) {
            EpsilonAddonManager.INSTANCE.registerAddon(addon);
        }

        // 4. Initialize Epsilon core
        EpsilonClient.init();

        Constants.LOGGER.info("Epsilon initialized on Forge.");
    }

}

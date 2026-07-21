package com.github.epsilon.forge.addon;

import com.github.epsilon.forge.ForgePlatformAddon;
import net.minecraftforge.common.MinecraftForge;

/**
 * Registers Epsilon's built-in Forge addon via MinecraftForge.EVENT_BUS.
 */
public class ForgeSelfAddonRegistrar {

    private static boolean registered;

    private ForgeSelfAddonRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        MinecraftForge.EVENT_BUS.addListener(ForgeSelfAddonRegistrar::onAddonSetup);
    }

    private static void onAddonSetup(EpsilonAddonSetupEvent event) {
        event.registerAddon(ForgePlatformAddon.INSTANCE);
    }

}

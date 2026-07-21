package com.github.epsilon.forge.addon;

import com.github.epsilon.addon.EpsilonAddon;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge EVENT_BUS event for collecting Epsilon addons.
 */
public class EpsilonAddonSetupEvent extends Event {

    private final ArrayList<EpsilonAddon> addons = new ArrayList<>();

    public void registerAddon(EpsilonAddon addon) {
        if (addon != null) {
            addons.add(addon);
        }
    }

    public List<EpsilonAddon> getAddons() {
        return addons;
    }

}

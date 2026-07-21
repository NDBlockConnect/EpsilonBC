package com.github.epsilon.forge;

import com.github.epsilon.Constants;
import com.github.epsilon.addon.EpsilonAddon;

import java.util.List;

/**
 * Built-in Forge addon for Forge-only features.
 */
public class ForgePlatformAddon extends EpsilonAddon {

    public static final ForgePlatformAddon INSTANCE = new ForgePlatformAddon();

    private ForgePlatformAddon() {
        super("epsilon_forge");
    }

    @Override
    public void onSetup() {
        Constants.LOGGER.info("Forge platform addon initialized.");
    }

    @Override
    public String getDisplayName() {
        return "Forge Platform";
    }

    @Override
    public String getDescription() {
        return "Built-in addon for Forge-specific integrations.";
    }

    @Override
    public String getVersion() {
        return Constants.VERSION;
    }

    @Override
    public List<String> getAuthors() {
        return List.of("slmpc", "06789");
    }

}

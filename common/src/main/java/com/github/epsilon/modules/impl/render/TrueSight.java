package com.github.epsilon.modules.impl.render;

import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

public class TrueSight extends Module {

    public static final TrueSight INSTANCE = new TrueSight();

    private TrueSight() {
        super("TrueSight", Category.RENDER);
    }

    private final BoolSetting players = boolSetting("Players", true);
    private final BoolSetting creatures = boolSetting("Creatures", true);
    private final BoolSetting monsters = boolSetting("Monsters", true);
    private final BoolSetting ambients = boolSetting("Ambients", true);
    private final BoolSetting others = boolSetting("Others", true);

    // Called from MixinEntityRenderDispatcher to decide whether an invisible entity
    // should have its render-state invisibility flag cleared.
    public boolean shouldReveal(Entity entity) {
        if (entity instanceof Player) return players.getValue();

        MobCategory category = entity.getType().getCategory();
        return switch (category) {
            case CREATURE, WATER_CREATURE, AXOLOTLS, UNDERGROUND_WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

}

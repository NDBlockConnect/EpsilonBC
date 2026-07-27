package com.github.epsilon.modules.impl.render;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.graphics.schedulers.render3d.Render3DScheduler;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.ColorSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.*;

/**
 * Highlights the block directly beneath each entity's feet.
 * Useful in HvH to track where players/mobs are standing.
 */
public class FootBlock extends Module {

    public static final FootBlock INSTANCE = new FootBlock();

    private FootBlock() {
        super("Foot Block", Category.RENDER);
    }

    // --- Entity filters ---
    private final BoolSetting players   = boolSetting("Players",   true);
    private final BoolSetting friends   = boolSetting("Friends",   true);
    private final BoolSetting creatures = boolSetting("Creatures", false);
    private final BoolSetting monsters  = boolSetting("Monsters",  true);
    private final BoolSetting ambients  = boolSetting("Ambients",  false);
    private final BoolSetting others    = boolSetting("Others",    false);
    private final DoubleSetting range   = doubleSetting("Range", 64.0, 8.0, 256.0, 8.0);

    // --- Render mode ---
    private final BoolSetting filled  = boolSetting("Filled",  true);
    private final BoolSetting outline = boolSetting("Outline", true);

    // --- Fill colors per entity type (semi-transparent) ---
    private final ColorSetting playersColor   = colorSetting("Players Color",   new Color(0x60FF9200, true), true);
    private final ColorSetting friendsColor   = colorSetting("Friends Color",   new Color(0x6030FF00, true), true);
    private final ColorSetting creaturesColor = colorSetting("Creatures Color", new Color(0x60A0A4A6, true), true);
    private final ColorSetting monstersColor  = colorSetting("Monsters Color",  new Color(0x60FF0000, true), true);
    private final ColorSetting ambientsColor  = colorSetting("Ambients Color",  new Color(0x607B00FF, true), true);
    private final ColorSetting othersColor    = colorSetting("Others Color",    new Color(0x60FF0062, true), true);

    // --- Outline color (shared across types) ---
    private final ColorSetting outlineColor = colorSetting("Outline Color", new Color(0xCCFFFFFF, true), true);

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;

        double rangeSq = range.getValue() * range.getValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (mc.player.distanceToSqr(entity) > rangeSq) continue;
            if (!shouldRender(livingEntity)) continue;

            BlockPos below = entity.blockPosition().below();
            BlockState state = mc.level.getBlockState(below);
            if (state.isAir()) continue;

            VoxelShape shape = state.getShape(mc.level, below);
            AABB box = shape.isEmpty() ? new AABB(below) : shape.bounds().move(below);

            Color fillColor = getEntityColor(livingEntity);
            if (filled.getValue())  Render3DScheduler.INSTANCE.addFilledBox(box, fillColor);
            if (outline.getValue()) Render3DScheduler.INSTANCE.addOutlineBox(box, outlineColor.getValue());
        }
    }

    private boolean shouldRender(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) return false;
        // Allies (incl. middle-click-marked mobs) are exempt from all enemy visuals.
        if (Managers.ALLY.isAlly(entity)) return false;

        if (entity instanceof Player player) {
            if (player == mc.player) return false;
            if (Managers.FRIEND.isFriend(player)) return friends.getValue();
            return players.getValue();
        }

        MobCategory category = entity.getType().getCategory();
        return switch (category) {
            case CREATURE, WATER_CREATURE, AXOLOTLS, UNDERGROUND_WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    private Color getEntityColor(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (Managers.FRIEND.isFriend(player)) return friendsColor.getValue();
            return playersColor.getValue();
        }

        MobCategory category = entity.getType().getCategory();
        return switch (category) {
            case CREATURE, WATER_CREATURE, AXOLOTLS, UNDERGROUND_WATER_CREATURE -> creaturesColor.getValue();
            case MONSTER -> monstersColor.getValue();
            case AMBIENT, WATER_AMBIENT -> ambientsColor.getValue();
            default -> othersColor.getValue();
        };
    }
}

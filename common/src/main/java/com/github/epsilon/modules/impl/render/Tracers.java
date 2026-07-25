package com.github.epsilon.modules.impl.render;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.graphics.schedulers.render3d.Render3DScheduler;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.ColorSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.utils.render.WorldToScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class Tracers extends Module {

    public static final Tracers INSTANCE = new Tracers();

    public enum Target {
        Head,
        Body,
        Feet
    }

    private Tracers() {
        super("Tracers", Category.RENDER);
    }

    private final BoolSetting players = boolSetting("Players", true);
    private final BoolSetting friends = boolSetting("Friends", true);
    private final BoolSetting creatures = boolSetting("Creatures", false);
    private final BoolSetting monsters = boolSetting("Monsters", false);
    private final BoolSetting ambients = boolSetting("Ambients", false);
    private final BoolSetting others = boolSetting("Others", false);

    private final EnumSetting<Target> target = enumSetting("Target", Target.Body);
    private final DoubleSetting thickness = doubleSetting("Thickness", 1.5, 0.5, 5.0, 0.1);
    private final DoubleSetting distance = doubleSetting("Distance", 64.0, 4.0, 256.0, 1.0);

    private final ColorSetting playersColor = colorSetting("Players Color", new Color(0xFF9200), false);
    private final ColorSetting friendsColor = colorSetting("Friends Color", new Color(0x30FF00), false);
    private final ColorSetting creaturesColor = colorSetting("Creatures Color", new Color(0xA0A4A6), false);
    private final ColorSetting monstersColor = colorSetting("Monsters Color", new Color(0xFF0000), false);
    private final ColorSetting ambientsColor = colorSetting("Ambients Color", new Color(0x7B00FF), false);
    private final ColorSetting othersColor = colorSetting("Others Color", new Color(0xFF0062), false);

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();

        // Anchor the tracer origin just in front of the camera so it fans out from the crosshair
        // instead of clipping at the eye.
        Vec3 start = cameraPos.add(mc.player.getViewVector(partialTick));

        double maxDistanceSq = distance.getValue() * distance.getValue();
        float lineWidth = thickness.getValue().floatValue();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity livingEntity) || !shouldRender(livingEntity)) continue;
            if (cameraPos.distanceToSqr(entity.position()) > maxDistanceSq) continue;

            Vec3 interpolated = WorldToScreen.interpolate(entity, partialTick);
            Vec3 end = switch (target.getValue()) {
                case Head -> interpolated.add(0.0, entity.getBbHeight(), 0.0);
                case Body -> interpolated.add(0.0, entity.getBbHeight() / 2.0, 0.0);
                case Feet -> interpolated;
            };

            Render3DScheduler.INSTANCE.addLine(start, end, getEntityColor(livingEntity), lineWidth);
        }
    }

    private boolean shouldRender(Entity entity) {
        if (mc.player == null) return false;
        if (entity == mc.player || !entity.isAlive() || entity.isSpectator()) return false;
        // Allies (incl. middle-click-marked mobs) are exempt from all enemy visuals.
        if (Managers.ALLY.isAlly(entity)) return false;

        if (entity instanceof Player player) {
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

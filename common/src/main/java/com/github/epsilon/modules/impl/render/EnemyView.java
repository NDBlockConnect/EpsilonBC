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
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.render.WorldToScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

/**
 * Re-skins hostile players/entities so only what matters in a fight is visible.
 *
 * <ul>
 *   <li><b>Box</b> — hide the vanilla model and draw a wireframe hitbox (feature ①: hide the
 *       enemy but keep their collision box visible).</li>
 *   <li><b>Solid</b> — hide the model and draw a filled + outlined box.</li>
 *   <li><b>Glow</b> — keep the model but paint a colored outline glow on it (piggybacks on the
 *       same vanilla entity-outline pass {@link Shaders} uses via {@code state.outlineColor}).</li>
 * </ul>
 *
 * Allies (marked via {@code AllyMarker}/{@link com.github.epsilon.managers.impl.AllyManager}) and
 * friends are always exempt — this is an enemy-only visual, matching the rule that combat and
 * visual features never target allies. The model-hiding and glow logic is invoked from the
 * already-validated {@code MixinEntityRenderer#shouldRender} and
 * {@code MixinBaseEntityRenderer#extractRenderState} hooks; the Box/Solid boxes are drawn here on
 * {@link Render3DEvent}.
 */
public class EnemyView extends Module {

    public static final EnemyView INSTANCE = new EnemyView();

    public enum Style {
        Box,
        Solid,
        Glow
    }

    private EnemyView() {
        super("Enemy View", Category.RENDER);
    }

    public final EnumSetting<Style> style = enumSetting("Style", Style.Box);
    private final BoolSetting hideModel = boolSetting("Hide Model", true, () -> !style.is(Style.Glow));
    private final BoolSetting outline = boolSetting("Outline", true, () -> !style.is(Style.Glow));
    private final DoubleSetting thickness = doubleSetting("Line Thickness", 1.5, 0.5, 5.0, 0.1,
            () -> outline.getValue() && !style.is(Style.Glow));
    private final IntSetting fillAlpha = intSetting("Fill Alpha", 60, 0, 255, 1, () -> style.is(Style.Solid));

    private final BoolSetting players = boolSetting("Players", true);
    private final BoolSetting creatures = boolSetting("Creatures", false);
    private final BoolSetting monsters = boolSetting("Monsters", false);
    private final BoolSetting ambients = boolSetting("Ambients", false);
    private final BoolSetting others = boolSetting("Others", false);

    private final IntSetting maxRange = intSetting("Max Range", 96, 16, 256, 1);

    private final ColorSetting playersColor = colorSetting("Players Color", new Color(0xFF9200), false);
    private final ColorSetting creaturesColor = colorSetting("Creatures Color", new Color(0xA0A4A6), false);
    private final ColorSetting monstersColor = colorSetting("Monsters Color", new Color(0xFF0000), false);
    private final ColorSetting ambientsColor = colorSetting("Ambients Color", new Color(0x7B00FF), false);
    private final ColorSetting othersColor = colorSetting("Others Color", new Color(0xFF0062), false);

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;
        // Glow is applied on the render state (see shouldGlow); nothing to schedule in 3D here.
        if (style.is(Style.Glow)) return;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        float lineWidth = thickness.getValue().floatValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!shouldRenderReplacement(entity)) continue;

            AABB box = interpolatedBox(entity, partialTick);
            Color color = colorFor(entity);

            if (style.is(Style.Solid)) {
                Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha.getValue());
                Render3DScheduler.INSTANCE.addFilledBox(box, fill);
            }
            if (outline.getValue()) {
                Render3DScheduler.INSTANCE.addOutlineBox(box, color.getRGB(), lineWidth);
            }
        }
    }

    private AABB interpolatedBox(Entity entity, float partialTick) {
        Vec3 pos = WorldToScreen.interpolate(entity, partialTick);
        double halfWidth = entity.getBbWidth() / 2.0;
        double height = entity.getBbHeight();
        return new AABB(
                pos.x - halfWidth, pos.y, pos.z - halfWidth,
                pos.x + halfWidth, pos.y + height, pos.z + halfWidth);
    }

    // --- Invoked from the render mixins ---

    /** True when the enemy's vanilla model should be culled (Box/Solid styles with Hide Model). */
    public boolean shouldHideModel(Entity entity) {
        return isEnabled()
                && hideModel.getValue()
                && !style.is(Style.Glow)
                && hasVisibleReplacement()
                && shouldRenderReplacement(entity);
    }

    private boolean hasVisibleReplacement() {
        return outline.getValue() || style.is(Style.Solid) && fillAlpha.getValue() > 0;
    }

    private boolean shouldRenderReplacement(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.isRemoved() || !isTarget(entity)) return false;
        double maxSq = maxRange.getValue() * (double) maxRange.getValue();
        return mc.player.distanceToSqr(entity) <= maxSq;
    }

    /** True when the enemy should be painted with a glow outline instead of a box. */
    public boolean shouldGlow(Entity entity) {
        return isEnabled() && style.is(Style.Glow) && isTarget(entity);
    }

    /** ARGB glow color for the given enemy, matching its category color. */
    public int glowColor(Entity entity) {
        return colorFor(entity).getRGB();
    }

    // --- Classification ---

    private boolean isTarget(Entity entity) {
        if (mc.player == null) return false;
        if (entity == mc.player || !entity.isAlive() || entity.isSpectator()) return false;
        // Allies (incl. marked mobs) and friends are never enemies.
        if (Managers.ALLY.isAlly(entity)) return false;

        if (entity instanceof Player player) {
            if (Managers.FRIEND.isFriend(player)) return false;
            return players.getValue();
        }

        return switch (entity.getType().getCategory()) {
            case CREATURE, WATER_CREATURE, AXOLOTLS, UNDERGROUND_WATER_CREATURE -> creatures.getValue();
            case MONSTER -> monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> ambients.getValue();
            default -> others.getValue();
        };
    }

    private Color colorFor(Entity entity) {
        if (entity instanceof Player) return playersColor.getValue();
        return switch (entity.getType().getCategory()) {
            case CREATURE, WATER_CREATURE, AXOLOTLS, UNDERGROUND_WATER_CREATURE -> creaturesColor.getValue();
            case MONSTER -> monstersColor.getValue();
            case AMBIENT, WATER_AMBIENT -> ambientsColor.getValue();
            default -> othersColor.getValue();
        };
    }
}

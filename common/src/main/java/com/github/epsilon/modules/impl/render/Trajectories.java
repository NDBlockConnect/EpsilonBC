package com.github.epsilon.modules.impl.render;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.ColorSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class Trajectories extends Module {

    public static final Trajectories INSTANCE = new Trajectories();

    private Trajectories() {
        super("Trajectories", Category.RENDER);
    }

    private final IntSetting maxSteps = intSetting("Max Steps", 300, 20, 1000, 10);
    private final DoubleSetting thickness = doubleSetting("Thickness", 1.5, 0.5, 5.0, 0.1);
    private final BoolSetting playerVelocity = boolSetting("Add Player Velocity", true);
    private final BoolSetting entityHits = boolSetting("Entity Collision", true);
    private final BoolSetting landingBox = boolSetting("Landing Box", true);

    private final ColorSetting pathColor = colorSetting("Path Color", new Color(0x30FF00), false);
    private final ColorSetting boxColor = colorSetting("Box Color", new Color(0x80FF3C00, true), () -> landingBox.getValue());

    // Simple immutable holder for the physics constants of each projectile family.
    private record ProjectileInfo(double velocity, double gravity, double drag, double waterDrag, double roll) {}

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        ProjectileInfo info = getHeldProjectile();
        return info == null ? "None" : String.format("%.1f", info.velocity());
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;

        ProjectileInfo info = getHeldProjectile();
        if (info == null) return;

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();

        // shootFromRotation only offsets the vertical component by roll, keeping the horizontal
        // heading on the base pitch. This matches vanilla potion / xp bottle lob arcs exactly.
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double pitchRollRad = Math.toRadians(pitch + info.roll());
        Vec3 dir = new Vec3(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRollRad),
                Math.cos(yawRad) * Math.cos(pitchRad)
        ).normalize();

        Vec3 motion = dir.scale(info.velocity());
        if (playerVelocity.getValue()) {
            Vec3 pv = mc.player.getDeltaMovement();
            motion = motion.add(pv.x, mc.player.onGround() ? 0.0 : pv.y, pv.z);
        }

        Vec3 pos = mc.player.getEyePosition(partialTick);

        List<Entity> candidates = entityHits.getValue()
                ? mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(128.0),
                        e -> e != mc.player && e.isPickable() && !e.isSpectator())
                : List.of();

        Color lineColor = pathColor.getValue();
        float lineWidth = thickness.getValue().floatValue();

        Vec3 landing = null;

        for (int step = 0; step < maxSteps.getValue(); step++) {
            Vec3 next = pos.add(motion);
            Vec3 segEnd = next;
            boolean stop = false;

            // Block collision terminates the arc where a projectile would embed.
            HitResult hit = mc.level.clip(new ClipContext(
                    pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
            if (hit.getType() != HitResult.Type.MISS) {
                segEnd = hit.getLocation();
                stop = true;
            }

            // Entity intercept: keep the closest hit along this segment.
            if (!candidates.isEmpty()) {
                double bestSq = pos.distanceToSqr(segEnd);
                Vec3 entityHit = null;
                for (Entity entity : candidates) {
                    AABB box = entity.getBoundingBox().inflate(0.3);
                    Optional<Vec3> clip = box.clip(pos, segEnd);
                    if (clip.isPresent()) {
                        double d = pos.distanceToSqr(clip.get());
                        if (d < bestSq) {
                            bestSq = d;
                            entityHit = clip.get();
                        }
                    }
                }
                if (entityHit != null) {
                    segEnd = entityHit;
                    stop = true;
                }
            }

            Managers.GRAPHICS.getRender3DScheduler().addLine(pos, segEnd, lineColor, lineWidth);
            landing = segEnd;

            if (stop) break;

            pos = segEnd;

            double drag = isInWater(pos) ? info.waterDrag() : info.drag();
            motion = motion.scale(drag).subtract(0.0, info.gravity(), 0.0);
        }

        if (landingBox.getValue() && landing != null) {
            double s = 0.3;
            AABB box = new AABB(
                    landing.x - s, landing.y - s, landing.z - s,
                    landing.x + s, landing.y + s, landing.z + s);
            Managers.GRAPHICS.getRender3DScheduler().addOutlineBox(box, boxColor.getValue(), lineWidth);
        }
    }

    private boolean isInWater(Vec3 pos) {
        return !mc.level.getFluidState(BlockPos.containing(pos.x, pos.y, pos.z)).isEmpty();
    }

    private ProjectileInfo getHeldProjectile() {
        ProjectileInfo info = fromStack(mc.player.getMainHandItem());
        return info != null ? info : fromStack(mc.player.getOffhandItem());
    }

    private ProjectileInfo fromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Item item = stack.getItem();

        if (item instanceof BowItem) {
            float charge;
            if (mc.player.isUsingItem() && mc.player.getUseItem() == stack) {
                charge = BowItem.getPowerForTime(mc.player.getTicksUsingItem());
            } else {
                charge = 1.0f;
            }
            if (charge < 0.1f) charge = 1.0f;
            return new ProjectileInfo(charge * 3.0, 0.05, 0.99, 0.6, 0.0);
        }
        if (item instanceof CrossbowItem) return new ProjectileInfo(3.15, 0.05, 0.99, 0.6, 0.0);
        if (item instanceof TridentItem) return new ProjectileInfo(2.5, 0.05, 0.99, 0.6, 0.0);
        if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderpearlItem)
            return new ProjectileInfo(1.5, 0.03, 0.99, 0.8, 0.0);
        if (item instanceof ExperienceBottleItem) return new ProjectileInfo(0.7, 0.07, 0.99, 0.8, -20.0);
        if (item instanceof ThrowablePotionItem) return new ProjectileInfo(0.5, 0.05, 0.99, 0.8, -20.0);

        return null;
    }

}

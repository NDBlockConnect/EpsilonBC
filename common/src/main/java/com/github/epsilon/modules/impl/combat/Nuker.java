package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.graphics.schedulers.render3d.Render3DScheduler;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.ColorSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.EnchantmentUtils;
import com.github.epsilon.utils.player.InvUtils;
import com.github.epsilon.utils.rotation.RotationUtils;
import com.github.epsilon.utils.timer.TimerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Nuker extends Module {

    public static final Nuker INSTANCE = new Nuker();

    private Nuker() {
        super("Nuker", Category.COMBAT);
    }

    public enum Mode {
        // Progressive vanilla mining, one block at a time (server-legal on anticheat servers).
        Normal,
        // Instant packet START+STOP break on several blocks per pass.
        Packet
    }

    public enum Shape {
        // Any block within range.
        Sphere,
        // Only blocks at or below the player's feet level (safe flattening).
        Flat
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Normal);
    private final EnumSetting<Shape> shape = enumSetting("Shape", Shape.Sphere);
    private final DoubleSetting range = doubleSetting("Range", 4.5, 1.0, 6.0, 0.1);
    private final IntSetting maxBlocks = intSetting("Max Blocks", 4, 1, 64, 1, () -> mode.is(Mode.Packet));
    private final IntSetting delay = intSetting("Delay", 0, 0, 500, 10);
    private final BoolSetting onlyExposed = boolSetting("Only Exposed", false);
    private final BoolSetting autoTool = boolSetting("Auto Tool", true);
    private final BoolSetting swing = boolSetting("Swing", true);
    private final BoolSetting render = boolSetting("Render", true);
    private final ColorSetting sideColor = colorSetting("Side Color", new Color(255, 50, 50, 40), render::getValue);
    private final ColorSetting lineColor = colorSetting("Line Color", new Color(255, 50, 50, 180), render::getValue);

    private final TimerUtils timer = new TimerUtils();
    private final List<BlockPos> rendered = new ArrayList<>();
    private BlockPos current;

    @Override
    protected void onDisable() {
        current = null;
        rendered.clear();
    }

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.valueOf(rendered.size());
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;

        List<BlockPos> targets = collectTargets();
        rendered.clear();
        rendered.addAll(targets);

        if (targets.isEmpty()) {
            current = null;
            return;
        }

        if (!timer.passedMillise(delay.getValue())) return;

        if (mode.is(Mode.Packet)) {
            int count = Math.min(maxBlocks.getValue(), targets.size());
            for (int i = 0; i < count; i++) {
                instantBreak(targets.get(i));
            }
            timer.reset();
        } else {
            // Progressive: keep hammering the closest block until it breaks, then move on.
            if (current == null || !isBreakable(current)) {
                current = targets.get(0);
            }
            if (autoTool.getValue()) swapTool(current);
            Direction direction = RotationUtils.getDirection(current);
            mc.gameMode.continueDestroyBlock(current, direction);
            if (swing.getValue()) mc.player.swing(InteractionHand.MAIN_HAND);
            timer.reset();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.getValue() || rendered.isEmpty()) return;
        for (BlockPos pos : rendered) {
            AABB box = new AABB(pos);
            Render3DScheduler.INSTANCE.addFilledBox(box, sideColor.getValue());
            Render3DScheduler.INSTANCE.addOutlineBox(box, lineColor.getValue());
        }
    }

    private void instantBreak(BlockPos pos) {
        if (autoTool.getValue()) swapTool(pos);
        Direction direction = RotationUtils.getDirection(pos);
        mc.getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction));
        mc.getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
        if (swing.getValue()) mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.destroyBlock(pos);
    }

    private List<BlockPos> collectTargets() {
        List<BlockPos> list = new ArrayList<>();
        double r = range.getValue();
        double rSq = r * r;
        Vec3 eye = mc.player.getEyePosition();

        int feetY = Mth.floor(mc.player.getY());
        int cx = Mth.floor(mc.player.getX());
        int cy = Mth.floor(mc.player.getY());
        int cz = Mth.floor(mc.player.getZ());
        int ir = Mth.ceil(r);

        for (int x = -ir; x <= ir; x++) {
            for (int y = -ir; y <= ir; y++) {
                for (int z = -ir; z <= ir; z++) {
                    BlockPos pos = new BlockPos(cx + x, cy + y, cz + z);
                    if (shape.is(Shape.Flat) && pos.getY() > feetY) continue;
                    if (eye.distanceToSqr(Vec3.atCenterOf(pos)) > rSq) continue;
                    if (!isBreakable(pos)) continue;
                    if (onlyExposed.getValue() && !isExposed(pos)) continue;
                    list.add(pos);
                }
            }
        }

        list.sort(Comparator.comparingDouble(p -> eye.distanceToSqr(Vec3.atCenterOf(p))));
        return list;
    }

    private boolean isBreakable(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return false;
        if (!mc.player.isCreative() && state.getDestroySpeed(mc.level, pos) < 0) return false;
        return state.getCollisionShape(mc.level, pos) != Shapes.empty();
    }

    private boolean isExposed(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (mc.level.getBlockState(pos.relative(direction)).isAir()) return true;
        }
        return false;
    }

    private void swapTool(BlockPos pos) {
        int slot = bestTool(pos);
        if (slot != -1 && slot != mc.player.getInventory().getSelectedSlot()) {
            InvUtils.swap(slot, false);
        }
    }

    private int bestTool(BlockPos pos) {
        int index = -1;
        float fastest = 1.0f;
        BlockState state = mc.level.getBlockState(pos);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            float digSpeed = EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
            float destroySpeed = stack.getDestroySpeed(state);
            if (digSpeed + destroySpeed > fastest) {
                fastest = digSpeed + destroySpeed;
                index = i;
            }
        }
        return index;
    }

}

package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PacketEvent;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.combat.DamageUtils;
import com.github.epsilon.utils.player.ClickSlotUtils;
import com.github.epsilon.utils.player.FallingPlayer;
import com.github.epsilon.utils.player.InvHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 自动图腾（重写自 Meteor 参考）：
 * Strict 模式始终持有图腾；Smart 模式基于"当前血量 - 预测伤害"判断，
 * 预测源包括末影水晶/重生锚爆炸与下落轨迹伤害。图腾弹出包会重置延迟计时。
 */
public class AutoTotem extends Module {

    public static final AutoTotem INSTANCE = new AutoTotem();

    private enum Mode {
        Strict,
        Smart
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Strict);
    private final IntSetting delay = intSetting("Delay", 0, 0, 10, 1);
    private final DoubleSetting health = doubleSetting("Health", 16.0, 0.0, 36.0, 0.5, () -> mode.getValue() == Mode.Smart);
    private final BoolSetting elytra = boolSetting("Elytra", true, () -> mode.getValue() == Mode.Smart);
    private final BoolSetting explosionCheck = boolSetting("Explosion", true, () -> mode.getValue() == Mode.Smart);
    private final BoolSetting fallCheck = boolSetting("Fall", true, () -> mode.getValue() == Mode.Smart);
    private final BoolSetting gappleCheck = boolSetting("Gapple", true, () -> mode.getValue() == Mode.Smart);

    private boolean locked;
    private int ticks;

    private AutoTotem() {
        super("Auto Totem", Category.COMBAT);
    }

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.valueOf(InvHelper.getItemCount(Items.TOTEM_OF_UNDYING));
    }

    /**
     * 供其他模块查询：AutoTotem 是否处于"必须持有图腾"状态
     * （如 CrystalAura 可据此暂停放置以避免自爆）。
     */
    public boolean isLocked() {
        return isEnabled() && locked;
    }

    @EventHandler(priority = 200)
    public void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.gameMode == null) return;

        int totems = InvHelper.getItemCount(Items.TOTEM_OF_UNDYING);
        if (totems <= 0) {
            locked = false;
            return;
        }

        if (ticks < delay.getValue()) {
            ticks++;
            return;
        }

        boolean low = false;
        boolean ely = false;
        boolean gapple = false;

        if (mode.getValue() == Mode.Smart) {
            float predicted = possibleHealthReductions();
            low = mc.player.getHealth() + mc.player.getAbsorptionAmount() - predicted <= health.getValue().floatValue();
            ely = elytra.getValue()
                    && mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)
                    && mc.player.isFallFlying();
            gapple = gappleCheck.getValue() && isHoldingGapple();
        }

        locked = mode.getValue() == Mode.Strict || (mode.getValue() == Mode.Smart && (low || ely || gapple)) || mc.player.getY() < mc.level.getMinY() - 8.0;

        if (locked && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            int slot = InvHelper.getItemSlot(Items.TOTEM_OF_UNDYING);
            if (slot != -1) {
                moveItemToOffhand(slot);
            }
        }

        ticks = 0;
    }

    /**
     * 图腾弹出（EntityEvent.PROTECTED_FROM_DEATH = 35）时重置延迟计时，
     * 让弹图腾后的补图腾动作立即执行。
     */
    @EventHandler(priority = 100)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (!(event.getPacket() instanceof ClientboundEntityEventPacket packet)) return;
        if (packet.getEventId() != EntityEvent.PROTECTED_FROM_DEATH) return;
        if (packet.getEntity(mc.level) == null || !packet.getEntity(mc.level).equals(mc.player)) return;

        ticks = 0;
    }

    /**
     * 预测即将到来的伤害（取各项最大值）：
     * 1) 周边末影晶体爆炸（半径 12 范围内扫描）
     * 2) 充能重生锚爆炸（4 格内扫描）
     * 3) 下落轨迹伤害（FallingPlayer 模拟，20 tick 内落点）
     */
    private float possibleHealthReductions() {
        float worst = 0.0f;

        if (explosionCheck.getValue()) {
            // 末影晶体
            List<EndCrystal> crystals = mc.level.getEntitiesOfClass(
                    EndCrystal.class,
                    mc.player.getBoundingBox().inflate(12.0)
            );
            for (EndCrystal crystal : crystals) {
                float dmg = DamageUtils.rawExplosionDamage(mc.player, crystal.position(), DamageUtils.CRYSTAL_EXPLOSION_RADIUS);
                if (dmg > worst) worst = dmg;
            }

            // 充能重生锚
            BlockPos min = BlockPos.containing(mc.player.position().subtract(4.0, 2.0, 4.0));
            BlockPos max = BlockPos.containing(mc.player.position().add(4.0, 3.0, 4.0));
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                BlockState state = mc.level.getBlockState(pos);
                if (state.getBlock() != Blocks.RESPAWN_ANCHOR) continue;
                if (state.getValue(RespawnAnchorBlock.CHARGE) <= 0) continue;
                Vec3 center = Vec3.atCenterOf(pos);
                float dmg = DamageUtils.rawExplosionDamage(mc.player, center, DamageUtils.ANCHOR_EXPLOSION_RADIUS);
                if (dmg > worst) worst = dmg;
            }
        }

        if (fallCheck.getValue() && !mc.player.onGround() && mc.player.getDeltaMovement().y < -0.5 && !mc.player.isFallFlying()) {
            FallingPlayer falling = new FallingPlayer(mc.player);
            BlockPos landing = falling.findCollision(20);
            if (landing != null) {
                double fallDistance = mc.player.getY() - (landing.getY() + 1.0);
                if (fallDistance > 3.0) {
                    // 原版摔落伤害公式：(坠落格数 - 3)
                    float dmg = (float) fallDistance - 3.0f;
                    if (dmg > worst) worst = dmg;
                }
            }
        }

        return worst;
    }

    private boolean isHoldingGapple() {
        return mc.player.getMainHandItem().is(Items.GOLDEN_APPLE)
                || mc.player.getMainHandItem().is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    private void moveItemToOffhand(int slot) {
        if (slot < 9) {
            slot += 36;
        }

        if (mode.getValue() != Mode.Strict) {
            ClickSlotUtils.swap(slot, 40);
            return;
        }

        ClickSlotUtils.click(slot);
        ClickSlotUtils.click(45);

        if (!mc.player.inventoryMenu.getCarried().isEmpty()) {
            ClickSlotUtils.click(slot);
        }
    }

}

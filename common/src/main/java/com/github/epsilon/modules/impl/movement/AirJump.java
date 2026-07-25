package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.ClientTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;

/**
 * 允许玩家在空中重新起跳。行为：
 *  - 每次落地重置计数
 *  - 空中按下跳跃键（边沿触发，不是持续按）消耗一次余量，给一次接近 vanilla 起跳的 Y 冲量
 *  - Max Jumps=0 视为无限升天，配合按住跳跃键可以持续爬升（受 Cooldown 限速）
 *
 * 不改 vanilla 首跳；只在离地之后接管。
 */
public class AirJump extends Module {

    public static final AirJump INSTANCE = new AirJump();

    private AirJump() {
        super("AirJump", Category.MOVEMENT);
    }

    // 0 = 无限次（配合 Hold Mode 就是升天）；默认给 5 次，一开箱就能连跳到位。
    private final IntSetting maxJumps = intSetting("Max Jumps", 5, 0, 999, 1);
    private final DoubleSetting jumpPower = doubleSetting("Jump Power", 0.42, 0.1, 1.5, 0.01);
    // 按住跳的最小间隔，别调太低会被反作弊挑出来
    private final IntSetting cooldown = intSetting("Cooldown", 3, 0, 20, 1);
    private final BoolSetting resetFall = boolSetting("Reset Fall", true);
    // 默认打开：按住跳直接升天，别再要求玩家一直反复按空格
    private final BoolSetting holdMode = boolSetting("Hold Mode", true);

    private boolean prevJumpDown;
    private int usedJumps;
    private int ticksUntilNext;

    @Override
    protected void onDisable() {
        prevJumpDown = false;
        usedJumps = 0;
        ticksUntilNext = 0;
    }

    @EventHandler
    private void onTick(ClientTickEvent.Pre event) {
        if (nullCheck()) return;

        // 原版飞行 / 鞘翅飞行时不介入，避免踩到 vanilla 已有的垂直控制
        Abilities abilities = mc.player.getAbilities();
        if (abilities.flying || mc.player.isFallFlying()) {
            prevJumpDown = mc.options.keyJump.isDown();
            return;
        }

        if (mc.player.onGround() || mc.player.onClimbable() || mc.player.isInWater() || mc.player.isInLava()) {
            usedJumps = 0;
            ticksUntilNext = 0;
            prevJumpDown = mc.options.keyJump.isDown();
            return;
        }

        if (ticksUntilNext > 0) ticksUntilNext--;

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean edge = jumpDown && !prevJumpDown;
        boolean trigger = holdMode.getValue() ? jumpDown : edge;
        prevJumpDown = jumpDown;

        if (!trigger) return;
        if (ticksUntilNext > 0) return;

        int limit = maxJumps.getValue();
        if (limit > 0 && usedJumps >= limit) return;

        // 跳跃提升药水（Vanilla LivingEntity.getJumpPower）加成
        double bonus = 0.0;
        if (mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            bonus = 0.1 * (mc.player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1);
        }
        double power = jumpPower.getValue() + bonus;

        Vec3 v = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(v.x, power, v.z);
        if (resetFall.getValue()) mc.player.resetFallDistance();

        usedJumps++;
        ticksUntilNext = cooldown.getValue();
    }
}

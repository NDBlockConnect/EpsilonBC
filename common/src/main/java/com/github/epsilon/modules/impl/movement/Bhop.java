package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.KeyboardInputEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.IntSetting;

/**
 * 自动连跳。原来的行为夹在 Strafe 里，容易在玩家没意识到的情况下狂跳。
 * 独立成模块后：开一次就是"按住前进=自动连跳"，关掉就完全不动手脚。
 */
public class Bhop extends Module {

    public static final Bhop INSTANCE = new Bhop();

    private Bhop() {
        super("Bhop", Category.MOVEMENT);
    }

    private final BoolSetting requireMoving = boolSetting("Require Moving", true);
    private final BoolSetting inWater = boolSetting("In Water", false);
    private final IntSetting delay = intSetting("Delay", 0, 0, 20, 1);

    private int cooldown;

    @Override
    protected void onDisable() {
        cooldown = 0;
    }

    @EventHandler
    private void onKeyboardInput(KeyboardInputEvent event) {
        if (nullCheck()) return;
        if (mc.player.getAbilities().flying || mc.player.isFallFlying()) return;
        if (!inWater.getValue() && (mc.player.isInWater() || mc.player.isInLava())) return;

        // 修复：只检查当前事件输入，不依赖现有速度（玩家静止首次起步时速度尚未形成）
        if (requireMoving.getValue()) {
            float forward = event.getForward();
            float strafe = event.getStrafe();
            if (forward == 0.0f && strafe == 0.0f) return;
        }

        if (!mc.player.onGround()) return;
        // 防止在攀爬、骑乘等特殊状态下触发
        if (mc.player.onClimbable() || mc.player.isPassenger()) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }
        event.setJump(true);
        cooldown = delay.getValue();
    }
}

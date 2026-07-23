package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.world.phys.Vec3;

public class AntiAFK extends Module {

    public static final AntiAFK INSTANCE = new AntiAFK();

    private AntiAFK() {
        super("AntiAFK", Category.PLAYER);
    }

    public enum Mode {
        Rotate,
        Jump,
        Sneak
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Rotate);
    private final IntSetting interval    = intSetting("Interval (ticks)", 200, 20, 6000, 20);

    private int tickCounter = 0;
    private int phase = 0;

    @Override
    protected void onEnable() {
        tickCounter = 0;
        phase = 0;
    }

    @Override
    protected void onDisable() {
        if (!nullCheck()) mc.player.setShiftKeyDown(false);
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (mc.player.isPassenger()) return;

        tickCounter++;
        if (tickCounter < interval.getValue()) return;
        tickCounter = 0;

        switch (mode.getValue()) {
            case Rotate -> {
                // Alternate yaw by ±15 degrees each trigger.
                float yaw = mc.player.getYRot() + (phase % 2 == 0 ? 15f : -15f);
                mc.player.setYRot(yaw);
                mc.player.yRotO = yaw;
                phase++;
            }
            case Jump -> {
                if (mc.player.onGround()) {
                    Vec3 v = mc.player.getDeltaMovement();
                    mc.player.setDeltaMovement(v.x, 0.42, v.z);
                }
            }
            case Sneak -> {
                // Toggle sneak for a couple ticks.
                mc.player.setShiftKeyDown(!mc.player.isShiftKeyDown());
            }
        }
    }
}

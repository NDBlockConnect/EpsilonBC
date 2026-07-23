package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.ClientTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import net.minecraft.world.phys.Vec3;

public class Jesus extends Module {

    public static final Jesus INSTANCE = new Jesus();

    private Jesus() {
        super("Jesus", Category.MOVEMENT);
    }

    public enum Mode {
        Water,
        Lava,
        Both
    }

    private final EnumSetting<Mode> mode     = enumSetting("Mode", Mode.Water);
    private final BoolSetting sneakToSink    = boolSetting("Sneak To Sink", true);

    @EventHandler
    private void onTick(ClientTickEvent.Pre event) {
        if (nullCheck()) return;
        if (sneakToSink.getValue() && mc.player.isCrouching()) return;
        if (mc.player.getAbilities().flying || mc.player.isFallFlying()) return;

        boolean inWater = mc.player.isInWater();
        boolean inLava  = mc.player.isInLava();
        boolean active  = switch (mode.getValue()) {
            case Water -> inWater;
            case Lava  -> inLava;
            case Both  -> inWater || inLava;
        };

        if (!active) return;
        // Allow swimming deeper when fully submerged.
        if (mc.player.isUnderWater()) return;

        Vec3 vel = mc.player.getDeltaMovement();
        if (vel.y < 0.0) {
            mc.player.setDeltaMovement(vel.x, 0.05, vel.z);
            mc.player.resetFallDistance();
        }
    }
}

package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.MoveEvent;
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
    private void onMove(MoveEvent event) {
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

        // Pin the player flat at the surface: zero the vertical delta so vanilla
        // buoyancy can't push us up and gravity can't drag us down. This removes
        // the bobbing that the old +0.05 delta nudge caused, so it feels like
        // walking on solid blocks. Horizontal movement is preserved.
        event.setY(0.0);
        event.cancel();

        Vec3 vel = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(vel.x, 0.0, vel.z);
        mc.player.resetFallDistance();
    }
}

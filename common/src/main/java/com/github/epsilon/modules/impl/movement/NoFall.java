package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.SendPositionEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;

public class NoFall extends Module {

    public static final NoFall INSTANCE = new NoFall();

    private NoFall() {
        super("No Fall", Category.MOVEMENT);
    }

    private enum Mode {
        GroundSpoof,
        Grim2B2T
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.GroundSpoof);
    private final DoubleSetting fallDistance = doubleSetting("Fall Distance", 3, 3, 16, 1, () -> mode.is(Mode.GroundSpoof));

    @EventHandler
    private void onSendPosition(SendPositionEvent event) {
        if (nullCheck() || !isFalling()) return;

        if (mode.is(Mode.Grim2B2T)) {
            // Nudge Y upward by a tiny epsilon — server sees upward movement and
            // resets its fall-distance counter without any ground-state desync.
            event.setY(event.getY() + 0.000000001);
            event.setOnGround(false);
        } else {
            // GroundSpoof: set onGround=true in the natural position packet.
            // Modifying it here (before PacketEvent.Send) means Blink will buffer
            // the already-corrected packet; when released the server sees the player
            // as always on the ground and never accumulates fall distance.
            event.setOnGround(true);
        }
        mc.player.resetFallDistance();
    }

    private boolean isFalling() {
        if (mc.player.isFallFlying()) {
            return false;
        }
        if (mode.is(Mode.Grim2B2T)) {
            return mc.player.fallDistance > 3f;
        }
        return mc.player.fallDistance > fallDistance.getValue();
    }

}

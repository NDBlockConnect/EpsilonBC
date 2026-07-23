package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.ClientTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

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
    private void onClientTick(ClientTickEvent.Pre event) {
        if (nullCheck() || !isFalling()) return;

        if (mode.is(Mode.Grim2B2T)) {
            // Send a tiny upward-offset packet — server sees upward movement and resets
            // its fall distance counter without any ground-state desync.
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.getX(), mc.player.getY() + 0.000000001, mc.player.getZ(),
                    mc.player.getYRot(), mc.player.getXRot(), false, mc.player.horizontalCollision));
        } else {
            // GroundSpoof: tell server player is on the ground at current position.
            // One packet per tick while falling; resetFallDistance() keeps client
            // fallDistance at 0 so the server never sees a lethal landing.
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYRot(), mc.player.getXRot(), true, mc.player.horizontalCollision));
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

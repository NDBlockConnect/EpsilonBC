package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Burrow extends Module {

    public static final Burrow INSTANCE = new Burrow();

    private Burrow() {
        super("Burrow", Category.MOVEMENT);
    }

    private final BoolSetting smart = boolSetting("Smart", true);
    private final BoolSetting onGroundOnly = boolSetting("On Ground Only", true);

    private boolean burrowed = false;

    @Override
    protected void onEnable() {
        burrowed = false;
    }

    @Override
    protected void onDisable() {
        burrowed = false;
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (onGroundOnly.getValue() && !mc.player.onGround()) return;

        if (smart.getValue()) {
            // Only burrow when there's a nearby threat (player within 8 blocks)
            boolean threat = mc.level.players().stream()
                    .anyMatch(p -> p != mc.player && p.distanceToSqr(mc.player) < 64.0);
            if (!threat) {
                burrowed = false;
                return;
            }
        }

        if (!burrowed) {
            // Send a packet moving the player down by 0.5 blocks to slip into the ground
            double x = mc.player.getX();
            double y = mc.player.getY() - 0.5;
            double z = mc.player.getZ();
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(x, y, z,
                    mc.player.getYRot(), mc.player.getXRot(), true, mc.player.horizontalCollision));
            burrowed = true;
        } else {
            // Restore position next tick
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(x, y, z,
                    mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision));
            burrowed = false;
        }
    }

}

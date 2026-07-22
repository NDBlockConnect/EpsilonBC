package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.ClientTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;

public class AutoRespawn extends Module {

    public static final AutoRespawn INSTANCE = new AutoRespawn();

    private AutoRespawn() {
        super("Auto Respawn", Category.PLAYER);
    }

    private final IntSetting delay = intSetting("Delay", 1, 0, 60, 1);

    private int clock;
    private boolean wasDead;

    @Override
    protected void onEnable() {
        clock = 0;
        wasDead = false;
    }

    @EventHandler
    private void onClientTick(ClientTickEvent.Pre event) {
        if (mc.player == null || mc.getConnection() == null) return;

        boolean dead = mc.player.isDeadOrDying() || mc.player.getHealth() <= 0.0f;

        if (!dead) {
            wasDead = false;
            clock = 0;
            return;
        }

        if (!wasDead) {
            wasDead = true;
            clock = 0;
        }

        if (clock < delay.getValue()) {
            clock++;
            return;
        }

        mc.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
        clock = 0;
    }

}

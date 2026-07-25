package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public class Regen extends Module {

    public static final Regen INSTANCE = new Regen();

    private Regen() {
        super("Regen", Category.COMBAT);
    }

    private final DoubleSetting health = doubleSetting("Health", 19.0, 1.0, 20.0, 0.5);
    private final IntSetting minHunger = intSetting("Min Hunger", 18, 0, 20, 1);
    private final IntSetting delay = intSetting("Delay", 1, 0, 20, 1);
    private final BoolSetting onlyGround = boolSetting("Only On Ground", false);

    private int clock;

    @Override
    protected void onEnable() {
        clock = 0;
    }

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.format("%.1f", mc.player.getHealth());
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.getConnection() == null) return;

        if (clock > 0) {
            clock--;
            return;
        }

        // Only worth swapping when we are actually missing health.
        if (mc.player.getHealth() >= Math.min(health.getValue().floatValue(), mc.player.getMaxHealth())) return;

        // Vanilla natural regen requires a fed hunger bar.
        if (mc.player.getFoodData().getFoodLevel() < minHunger.getValue()) return;

        if (onlyGround.getValue() && !mc.player.onGround()) return;

        int current = mc.player.getInventory().getSelectedSlot();
        int other = (current + 1) % 9;

        // Swap held-item packets force the server to re-tick the player, applying regen ASAP.
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(other));
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(current));

        clock = delay.getValue();
    }

}

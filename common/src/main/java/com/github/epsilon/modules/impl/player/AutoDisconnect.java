package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.utils.player.ChatUtils;
import net.minecraft.network.chat.Component;

public class AutoDisconnect extends Module {

    public static final AutoDisconnect INSTANCE = new AutoDisconnect();

    private AutoDisconnect() {
        super("Auto Disconnect", Category.PLAYER);
    }

    private final DoubleSetting health = doubleSetting("Health", 6.0, 0.5, 20.0, 0.5);
    private final BoolSetting onlyInCombat = boolSetting("Only Hurt", false);
    private final BoolSetting notify = boolSetting("Notify", true);

    private float lastHealth;

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            lastHealth = mc.player.getHealth();
        }
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.getConnection() == null) return;

        float currentHealth = mc.player.getHealth();
        float totalHealth = currentHealth + mc.player.getAbsorptionAmount();

        boolean tookDamage = currentHealth < lastHealth;
        lastHealth = currentHealth;

        if (totalHealth > health.getValue().floatValue()) return;
        if (onlyInCombat.getValue() && !tookDamage) return;

        String reason = "[EpsilonBC] Auto Disconnect: health " + String.format("%.1f", totalHealth);
        if (notify.getValue()) {
            ChatUtils.addChatMessage(reason);
        }

        mc.getConnection().getConnection().disconnect(Component.literal(reason));
        toggle();
    }

}

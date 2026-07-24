package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import net.minecraft.world.item.Items;

public class AutoCrystalSwitch extends Module {

    public static final AutoCrystalSwitch INSTANCE = new AutoCrystalSwitch();

    private AutoCrystalSwitch() {
        super("Auto Crystal Switch", Category.COMBAT);
    }

    private enum Strategy {
        CrystalToObsidian,
        Smart
    }

    private final EnumSetting<Strategy> strategy = enumSetting("Strategy", Strategy.CrystalToObsidian);
    private final BoolSetting switchBack = boolSetting("Switch Back", true);
    private final IntSetting delay = intSetting("Delay", 2, 0, 10, 1);

    private boolean wasHoldingCrystal = false;
    private int delayClock = 0;

    @Override
    protected void onEnable() {
        wasHoldingCrystal = false;
        delayClock = 0;
    }

    @Override
    protected void onDisable() {
        wasHoldingCrystal = false;
        delayClock = 0;
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;

        if (delayClock > 0) {
            delayClock--;
            return;
        }

        boolean holdingCrystal = mc.player.getMainHandItem().is(Items.END_CRYSTAL)
                || mc.player.getOffhandItem().is(Items.END_CRYSTAL);

        if (strategy.getValue() == Strategy.CrystalToObsidian) {
            // When the player stops holding a crystal (was holding it last tick), switch to obsidian
            if (wasHoldingCrystal && !holdingCrystal) {
                // Already switched away, nothing to do
            } else if (wasHoldingCrystal && holdingCrystal) {
                // Still holding crystal — check if CrystalAura is not crystalling anymore
                if (switchBack.getValue() && !CrystalAura.INSTANCE.crystalling) {
                    FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
                    if (obsidian.found()) {
                        InvUtils.swap(obsidian.slot(), false);
                        delayClock = delay.getValue();
                    }
                }
            }
        } else {
            // Smart: switch to crystal when crystalling, switch to obsidian when not
            if (CrystalAura.INSTANCE.isEnabled() && CrystalAura.INSTANCE.crystalling) {
                if (!holdingCrystal) {
                    FindItemResult crystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
                    if (crystal.found()) {
                        InvUtils.swap(crystal.slot(), switchBack.getValue());
                        delayClock = delay.getValue();
                    }
                }
            } else {
                if (holdingCrystal && switchBack.getValue()) {
                    FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
                    if (obsidian.found()) {
                        InvUtils.swap(obsidian.slot(), false);
                        delayClock = delay.getValue();
                    }
                }
            }
        }

        wasHoldingCrystal = holdingCrystal;
    }

}

package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import com.github.epsilon.utils.rotation.Priority;
import com.github.epsilon.utils.rotation.Rot2f;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class AutoPot extends Module {

    public static final AutoPot INSTANCE = new AutoPot();

    private AutoPot() {
        super("Auto Pot", Category.COMBAT);
    }

    private enum SwitchMode {
        Normal,
        Silent
    }

    private final EnumSetting<SwitchMode> switchMode = enumSetting("Switch Mode", SwitchMode.Silent);
    private final DoubleSetting health = doubleSetting("Health", 12.0, 1.0, 20.0, 0.5);
    private final BoolSetting onlyGround = boolSetting("Only On Ground", true);
    private final IntSetting delay = intSetting("Delay", 10, 0, 40, 1);
    private final BoolSetting swingHand = boolSetting("Swing Hand", false);

    private int clock;

    @Override
    protected void onEnable() {
        clock = 0;
    }

    @EventHandler
    private void onClientTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.gameMode == null) return;

        if (clock > 0) {
            clock--;
            return;
        }

        float totalHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (totalHealth > health.getValue().floatValue()) return;

        if (onlyGround.getValue() && !mc.player.onGround()) return;

        FindItemResult result = InvUtils.findInHotbar(this::isHealingSplash);
        if (!result.found()) return;

        // Aim straight down so the splash lands at our feet.
        Managers.ROTATION.setRotations(new Rot2f(mc.player.getYRot(), 90.0f), 180, Priority.High);

        InteractionHand hand;
        if (result.isOffhand()) {
            hand = InteractionHand.OFF_HAND;
        } else {
            InvUtils.swap(result.slot(), switchMode.is(SwitchMode.Silent));
            hand = InteractionHand.MAIN_HAND;
        }

        mc.gameMode.useItem(mc.player, hand);
        if (swingHand.getValue()) {
            mc.player.swing(hand);
        } else {
            mc.getConnection().send(new ServerboundSwingPacket(hand));
        }

        if (switchMode.is(SwitchMode.Silent) && !result.isOffhand()) {
            InvUtils.swapBack();
        }

        clock = delay.getValue();
    }

    private boolean isHealingSplash(ItemStack stack) {
        if (!stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) return false;

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().value().isBeneficial()) {
                return true;
            }
        }
        return false;
    }

}

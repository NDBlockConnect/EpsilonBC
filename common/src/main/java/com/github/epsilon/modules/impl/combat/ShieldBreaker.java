package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

public class ShieldBreaker extends Module {

    public static final ShieldBreaker INSTANCE = new ShieldBreaker();

    private ShieldBreaker() {
        super("Shield Breaker", Category.COMBAT);
    }

    private enum AxePriority {
        Damage,
        Speed
    }

    private final DoubleSetting range = doubleSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private final BoolSetting switchBack = boolSetting("Switch Back", true);
    private final BoolSetting onlyWhenShields = boolSetting("Only When Enemy Shields", true);
    private final EnumSetting<AxePriority> axePriority = enumSetting("Axe Priority", AxePriority.Damage);

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;

        Player target = findShieldingTarget();
        if (target == null) return;

        FindItemResult axeResult = findAxe();
        if (!axeResult.found()) return;

        InvUtils.swap(axeResult.slot(), true);

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (switchBack.getValue()) {
            InvUtils.swapBack();
        }
    }

    private Player findShieldingTarget() {
        Player best = null;
        double bestDistSq = range.getValue() * range.getValue();

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (onlyWhenShields.getValue() && !player.isBlocking()) continue;

            double distSq = mc.player.distanceToSqr(player);
            if (distSq > bestDistSq) continue;

            best = player;
            bestDistSq = distSq;
        }
        return best;
    }

    private FindItemResult findAxe() {
        // Both Damage and Speed modes: find the first axe in hotbar (prioritize netherite > diamond > others by slot order)
        return InvUtils.findInHotbar(stack -> !stack.isEmpty() && stack.is(ItemTags.AXES));
    }

}

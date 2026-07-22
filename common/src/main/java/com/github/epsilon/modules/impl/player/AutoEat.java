package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public class AutoEat extends Module {

    public static final AutoEat INSTANCE = new AutoEat();

    private AutoEat() {
        super("Auto Eat", Category.PLAYER);
    }

    private final IntSetting hunger = intSetting("Hunger", 16, 0, 20, 1);
    private final DoubleSetting health = doubleSetting("Health", 8.0, 0.0, 20.0, 0.5);
    private final BoolSetting avoidBadFood = boolSetting("Avoid Bad Food", true);
    private final BoolSetting gappleAtLowHp = boolSetting("Gapple At Low HP", true);

    private static final Set<Item> BAD_FOOD = Set.of(
            Items.ROTTEN_FLESH,
            Items.SPIDER_EYE,
            Items.POISONOUS_POTATO,
            Items.PUFFERFISH,
            Items.CHICKEN,
            Items.SUSPICIOUS_STEW,
            Items.CHORUS_FRUIT
    );

    private boolean eating;
    private boolean usedInvSwap;

    @Override
    protected void onEnable() {
        eating = false;
        usedInvSwap = false;
    }

    @Override
    protected void onDisable() {
        stopEating();
    }

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.valueOf(mc.player.getFoodData().getFoodLevel());
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.gameMode == null || mc.player.isSpectator() || mc.screen != null) {
            stopEating();
            return;
        }

        boolean want = shouldEat();

        if (eating) {
            if (!want || !isFood(mc.player.getMainHandItem())) {
                stopEating();
                return;
            }
            mc.options.keyUse.setDown(true);
            return;
        }

        if (!want) return;

        FindItemResult food = findFood();
        if (!food.found()) return;

        ItemStack stack = mc.player.getInventory().getItem(food.slot());
        if (!canEatNow(stack)) return;

        startEating(food);
    }

    private boolean shouldEat() {
        boolean hungry = mc.player.getFoodData().getFoodLevel() <= hunger.getValue();
        boolean lowHp = mc.player.getHealth() <= health.getValue().floatValue();
        return hungry || lowHp;
    }

    private FindItemResult findFood() {
        boolean lowHp = mc.player.getHealth() <= health.getValue().floatValue();

        if (lowHp && gappleAtLowHp.getValue()) {
            FindItemResult gap = InvUtils.find(this::isGolden, 0, 35);
            if (gap.found()) return gap;
        }

        FindItemResult normal = InvUtils.find(
                s -> isFood(s) && !isGolden(s) && (!avoidBadFood.getValue() || !isBadFood(s)), 0, 35);
        if (normal.found()) return normal;

        return InvUtils.find(s -> isFood(s) && (!avoidBadFood.getValue() || !isBadFood(s)), 0, 35);
    }

    private void startEating(FindItemResult food) {
        int slot = food.slot();
        if (slot >= 0 && slot < 9) {
            InvUtils.swap(slot, true);
            usedInvSwap = false;
        } else if (slot >= 9 && slot <= 35) {
            InvUtils.invSwap(slot);
            usedInvSwap = true;
        } else {
            return;
        }

        eating = true;
        mc.options.keyUse.setDown(true);
    }

    private void stopEating() {
        if (!eating) return;

        mc.options.keyUse.setDown(false);

        if (usedInvSwap) {
            InvUtils.invSwapBack();
        } else {
            InvUtils.swapBack();
        }

        usedInvSwap = false;
        eating = false;
    }

    private boolean isFood(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.get(DataComponents.FOOD) != null;
    }

    private boolean isGolden(ItemStack stack) {
        return isFood(stack) && (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE);
    }

    private boolean isBadFood(ItemStack stack) {
        return stack != null && BAD_FOOD.contains(stack.getItem());
    }

    private boolean canEatNow(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return false;
        if (mc.player.getFoodData().getFoodLevel() < 20) return true;
        return food.canAlwaysEat();
    }

}

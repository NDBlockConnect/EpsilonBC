package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.ClickSlotUtils;
import com.github.epsilon.utils.player.InvHelper;
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
    private int eatHotbarSlot = -1;
    private int prevSelectedSlot = -1;
    private int stashContainerSlot = -1;

    @Override
    protected void onEnable() {
        resetState();
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
            // Keep chewing until we no longer need to eat or the held item is no longer food.
            if (!want || !isFood(mc.player.getMainHandItem())) {
                stopEating();
                return;
            }
            mc.options.keyUse.setDown(true);
            return;
        }

        if (!want) return;

        int foodSlot = findBestFoodSlot();
        if (foodSlot == -1) return;

        startEating(foodSlot);
    }

    private boolean shouldEat() {
        boolean hungry = mc.player.getFoodData().getFoodLevel() <= hunger.getValue();
        boolean lowHp = mc.player.getHealth() <= health.getValue().floatValue();
        return hungry || lowHp;
    }

    /**
     * Chooses the most appropriate food slot (0-35, hotbar + main inventory).
     * Priorities: gapple when low HP -> food that best matches the hunger deficit.
     */
    private int findBestFoodSlot() {
        boolean lowHp = mc.player.getHealth() <= health.getValue().floatValue();

        if (lowHp && gappleAtLowHp.getValue()) {
            int enchanted = firstSlotOf(Items.ENCHANTED_GOLDEN_APPLE);
            if (enchanted != -1) return enchanted;
            int golden = firstSlotOf(Items.GOLDEN_APPLE);
            if (golden != -1) return golden;
        }

        int deficit = 20 - mc.player.getFoodData().getFoodLevel();
        if (deficit <= 0) deficit = 1;

        int coverSlot = -1;
        int coverNutrition = Integer.MAX_VALUE;
        float coverSaturation = -1.0f;

        int largestSlot = -1;
        int largestNutrition = -1;
        float largestSaturation = -1.0f;

        for (int i = 0; i <= 35; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!isFood(stack)) continue;
            if (avoidBadFood.getValue() && isBadFood(stack)) continue;

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) continue;
            if (!canEatNow(food)) continue;

            int nutrition = food.nutrition();
            float saturation = food.saturation();

            // Best food that fully covers the deficit with the least overshoot.
            if (nutrition >= deficit) {
                if (nutrition < coverNutrition || (nutrition == coverNutrition && saturation > coverSaturation)) {
                    coverNutrition = nutrition;
                    coverSaturation = saturation;
                    coverSlot = i;
                }
            }

            // Fallback: the most filling food when nothing fully covers the deficit.
            if (nutrition > largestNutrition || (nutrition == largestNutrition && saturation > largestSaturation)) {
                largestNutrition = nutrition;
                largestSaturation = saturation;
                largestSlot = i;
            }
        }

        return coverSlot != -1 ? coverSlot : largestSlot;
    }

    private int firstSlotOf(Item item) {
        for (int i = 0; i <= 35; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) return i;
        }
        return -1;
    }

    private void startEating(int foodSlot) {
        prevSelectedSlot = mc.player.getInventory().getSelectedSlot();

        if (foodSlot >= 0 && foodSlot < 9) {
            // Food already on the hotbar, just hold it.
            eatHotbarSlot = foodSlot;
            stashContainerSlot = -1;
            mc.player.getInventory().setSelectedSlot(foodSlot);
        } else {
            // Pull food out of the main inventory: use an empty hotbar slot if we have
            // one, otherwise swap it with whatever is currently held.
            int empty = InvHelper.findEmptySlot();
            int target = empty != -1 ? empty : prevSelectedSlot;
            int containerSlot = foodSlot; // main inventory slots 9-35 map 1:1 in the player menu

            ClickSlotUtils.swap(mc.player.inventoryMenu.containerId, containerSlot, target);
            eatHotbarSlot = target;
            stashContainerSlot = containerSlot;
            mc.player.getInventory().setSelectedSlot(target);
        }

        eating = true;
        mc.options.keyUse.setDown(true);
    }

    private void stopEating() {
        if (!eating) {
            mc.options.keyUse.setDown(false);
            return;
        }

        mc.options.keyUse.setDown(false);

        // Put the (possibly leftover) food stack back where it came from.
        if (stashContainerSlot != -1 && eatHotbarSlot != -1) {
            ClickSlotUtils.swap(mc.player.inventoryMenu.containerId, stashContainerSlot, eatHotbarSlot);
        }
        if (prevSelectedSlot != -1) {
            mc.player.getInventory().setSelectedSlot(prevSelectedSlot);
        }

        resetState();
    }

    private void resetState() {
        eating = false;
        eatHotbarSlot = -1;
        prevSelectedSlot = -1;
        stashContainerSlot = -1;
    }

    private boolean isFood(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.get(DataComponents.FOOD) != null;
    }

    private boolean isBadFood(ItemStack stack) {
        return stack != null && BAD_FOOD.contains(stack.getItem());
    }

    private boolean canEatNow(FoodProperties food) {
        if (mc.player.getFoodData().getFoodLevel() < 20) return true;
        return food.canAlwaysEat();
    }

}

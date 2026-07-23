package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.ClickSlotUtils;
import com.github.epsilon.utils.player.InvHelper;
import com.github.epsilon.utils.timer.TimerUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AutoReplace extends Module {

    public static final AutoReplace INSTANCE = new AutoReplace();

    private AutoReplace() {
        super("AutoReplace", Category.PLAYER);
    }

    private final IntSetting threshold = intSetting("Threshold", 4, 1, 32, 1);
    private final BoolSetting onlyBlocks = boolSetting("Only Blocks", false);
    private final BoolSetting swapTools = boolSetting("Refill Tools", true);
    private final IntSetting delay = intSetting("Delay", 100, 0, 1000, 50);

    private final TimerUtils timer = new TimerUtils();

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (InvHelper.shouldDisableFeatures()) return;

        // Never fight an open non-player container.
        if (mc.screen instanceof AbstractContainerScreen container
                && container.getMenu().containerId != mc.player.inventoryMenu.containerId) {
            return;
        }

        if (!timer.passedMillise(delay.getValue())) return;

        int selected = mc.player.getInventory().getSelectedSlot();
        ItemStack held = mc.player.getInventory().getItem(selected);
        if (held.isEmpty()) return;

        Item item = held.getItem();
        boolean stackable = held.getMaxStackSize() > 1;

        // Non-stackable tools/weapons: refill only when nearly broken (if enabled).
        if (!stackable) {
            if (!swapTools.getValue()) return;
            if (!held.isDamageableItem()) return;
            int remaining = held.getMaxDamage() - held.getDamageValue();
            // treat threshold as remaining-durability percent trigger (<=5%)
            if (remaining > held.getMaxDamage() * 0.05) return;
        } else {
            if (onlyBlocks.getValue() && !(item instanceof net.minecraft.world.item.BlockItem)) return;
            if (held.getCount() > threshold.getValue()) return;
        }

        // Find another matching stack elsewhere in the inventory to move onto the
        // held hotbar slot. Prefer the fullest matching stack, skipping the held one.
        int bestSlot = -1;
        int bestCount = -1;
        for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            if (slot == selected || slot >= 36) continue;
            ItemStack candidate = mc.player.getInventory().getItem(slot);
            if (candidate.isEmpty()) continue;
            if (candidate.getItem() != item) continue;
            if (candidate.getCount() > bestCount) {
                bestCount = candidate.getCount();
                bestSlot = slot;
            }
        }
        if (bestSlot == -1) return;

        // Hotbar candidate slots (0-8) map to inventory-menu slot +36.
        int containerSlot = bestSlot < 9 ? bestSlot + 36 : bestSlot;
        ClickSlotUtils.swap(mc.player.inventoryMenu.containerId, containerSlot, selected);
        timer.reset();
    }
}

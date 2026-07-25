package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.settings.impl.RegistryListSetting;
import com.github.epsilon.utils.player.ClickSlotUtils;
import com.github.epsilon.utils.player.InvHelper;
import com.github.epsilon.utils.timer.TimerUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AutoEject extends Module {

    public static final AutoEject INSTANCE = new AutoEject();

    private AutoEject() {
        super("AutoEject", Category.PLAYER);
    }

    private enum ListMode {
        Whitelist, // eject ONLY items on the list
        Blacklist  // eject everything EXCEPT items on the list
    }

    private final EnumSetting<ListMode> listMode = enumSetting("List Mode", ListMode.Whitelist);
    private final RegistryListSetting<Item> items = itemListSetting("Items", List.of());
    private final IntSetting delay = intSetting("Delay", 100, 0, 1000, 50);
    private final BoolSetting onlyInInventory = boolSetting("Only In Inventory", false);
    private final BoolSetting ignoreHotbar = boolSetting("Ignore Hotbar", true);

    private final TimerUtils timer = new TimerUtils();

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (InvHelper.shouldDisableFeatures()) return;

        // Only run while the player's own inventory is open when requested.
        if (onlyInInventory.getValue() && !(mc.gui.screen() instanceof InventoryScreen)) return;

        // Never fight another open container (chest, shulker, etc.).
        if (mc.gui.screen() instanceof AbstractContainerScreen container
                && container.getMenu().containerId != mc.player.inventoryMenu.containerId) {
            return;
        }

        if (!timer.passedMillise(delay.getValue())) return;

        for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            // Skip armor / offhand equipment slots.
            if (slot >= 36) continue;
            if (ignoreHotbar.getValue() && slot < 9) continue;

            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isEmpty()) continue;
            if (!shouldEject(stack.getItem())) continue;
            if (!InvHelper.isItemValid(stack)) continue;

            // Hotbar slots (0-8) map to inventory-menu slots itemSlot+36.
            int containerSlot = slot < 9 ? slot + 36 : slot;
            ClickSlotUtils.dropAll(containerSlot);
            timer.reset();
            return; // one drop per delay window keeps it anti-cheat friendly
        }
    }

    private boolean shouldEject(Item item) {
        boolean listed = items.getValue().contains(item);
        return listMode.getValue() == ListMode.Whitelist ? listed : !listed;
    }
}

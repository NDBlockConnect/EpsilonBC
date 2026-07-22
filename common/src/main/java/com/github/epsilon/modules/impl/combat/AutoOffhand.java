package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.utils.player.ClickSlotUtils;
import com.github.epsilon.utils.player.InvHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class AutoOffhand extends Module {

    public static final AutoOffhand INSTANCE = new AutoOffhand();

    private AutoOffhand() {
        super("Auto Offhand", Category.COMBAT);
    }

    private enum OffhandItem {
        Totem,
        GoldenApple,
        EnchantedGoldenApple
    }

    private final EnumSetting<OffhandItem> item = enumSetting("Item", OffhandItem.Totem);
    private final BoolSetting strict = boolSetting("Strict", true);
    private final BoolSetting onlyOnEmpty = boolSetting("Only When Empty", false);
    private final DoubleSetting health = doubleSetting("Health", 36.0, 0.0, 36.0, 0.5,
            () -> !onlyOnEmpty.getValue());

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.valueOf(InvHelper.getItemCount(targetItem()));
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck() || mc.gameMode == null) return;

        Item target = targetItem();

        if (mc.player.getOffhandItem().is(target)) return;

        if (onlyOnEmpty.getValue()) {
            if (!mc.player.getOffhandItem().isEmpty()) return;
        } else {
            float totalHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            boolean lowHealth = totalHealth <= health.getValue().floatValue();
            if (!lowHealth && !mc.player.getOffhandItem().isEmpty()) return;
        }

        int slot = InvHelper.getItemSlot(target);
        if (slot == -1) return;

        moveItemToOffhand(slot);
    }

    private Item targetItem() {
        return switch (item.getValue()) {
            case Totem -> Items.TOTEM_OF_UNDYING;
            case GoldenApple -> Items.GOLDEN_APPLE;
            case EnchantedGoldenApple -> Items.ENCHANTED_GOLDEN_APPLE;
        };
    }

    private void moveItemToOffhand(int slot) {
        if (slot < 9) {
            slot += 36;
        }

        if (!strict.getValue()) {
            ClickSlotUtils.swap(slot, 40);
            return;
        }

        ClickSlotUtils.click(slot);
        ClickSlotUtils.click(45);

        if (!mc.player.inventoryMenu.getCarried().isEmpty()) {
            ClickSlotUtils.click(slot);
        }
    }

}

package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.BoolSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;

public class BucketLand extends Module {

    public static final BucketLand INSTANCE = new BucketLand();

    private BucketLand() {
        super("BucketLand", Category.PLAYER);
    }

    private final DoubleSetting fallDistance = doubleSetting("Fall Distance", 10.0, 3.0, 30.0, 1.0);
    private final BoolSetting onlyOnGround   = boolSetting("Only On Ground", false);

    private int prevSlot = -1;
    private boolean used = false;

    @Override
    protected void onDisable() {
        restoreSlot();
        used = false;
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava()) {
            restoreSlot();
            used = false;
            return;
        }

        // Only trigger when falling fast enough and haven't placed yet.
        if (used) return;
        if (mc.player.fallDistance < fallDistance.getValue()) return;

        int bucketSlot = findWaterBucket();
        if (bucketSlot == -1) return;

        // Select the bucket slot.
        prevSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(bucketSlot);

        // Use item (place water bucket below).
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        used = true;
    }

    private int findWaterBucket() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.WATER_BUCKET) return i;
        }
        return -1;
    }

    private void restoreSlot() {
        if (prevSlot != -1) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            prevSlot = -1;
        }
    }
}

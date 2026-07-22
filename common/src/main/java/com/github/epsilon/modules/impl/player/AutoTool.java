package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.utils.player.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class AutoTool extends Module {

    public static final AutoTool INSTANCE = new AutoTool();

    private AutoTool() {
        super("Auto Tool", Category.PLAYER);
    }

    public enum Priority {
        Efficiency,
        Durability
    }

    private final EnumSetting<Priority> priority = enumSetting("Priority", Priority.Efficiency);
    private final BoolSetting swapBack = boolSetting("Swap Back", true);
    private final BoolSetting saveItem = boolSetting("Save Item", true);
    private final BoolSetting silent = boolSetting("Silent", false);
    private final BoolSetting echestSilk = boolSetting("Ender Chest Silk Touch", true);

    public static int itemIndex;
    private boolean swap;
    private long swapDelay;
    private final List<Integer> lastItem = new ArrayList<>();

    @EventHandler
    public void onClientTick(PlayerTickEvent.Pre event) {
        if (!(mc.hitResult instanceof BlockHitResult result)) return;

        BlockPos pos = result.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        int tool = getTool(pos);

        if (tool != -1 && mc.options.keyAttack.isDown()) {
            lastItem.add(mc.player.getInventory().getSelectedSlot());

            if (silent.getValue()) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(tool));
            } else {
                mc.player.getInventory().setSelectedSlot(tool);
            }

            itemIndex = tool;
            swap = true;

            swapDelay = System.currentTimeMillis();
        } else if (swap && !lastItem.isEmpty() && System.currentTimeMillis() >= swapDelay + 300 && swapBack.getValue()) {
            if (silent.getValue()) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(lastItem.get(0)));
            } else {
                mc.player.getInventory().setSelectedSlot(lastItem.get(0));
            }

            itemIndex = lastItem.get(0);
            lastItem.clear();
            swap = false;
        }
    }

    public int getTool(final BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (state.getBlock() instanceof AirBlock) return -1;

        boolean enderChest = state.getBlock() instanceof EnderChestBlock && echestSilk.getValue();

        int best = -1;
        // Efficiency ranking: the highest effective mining speed wins.
        float bestSpeed = 1.0f;
        // Durability ranking: prefer the lowest tier able to harvest, then the
        // stack with the most remaining durability so we do not snap a tool mid-dig.
        int bestTier = Integer.MAX_VALUE;
        int bestRemaining = -1;

        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (saveItem.getValue() && stack.isDamageableItem()
                    && stack.getMaxDamage() - stack.getDamageValue() <= 10) {
                continue;
            }

            // Ender chest special case: only a silk-touch tool is acceptable.
            if (enderChest && EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.SILK_TOUCH) <= 0) {
                continue;
            }

            float speed = effectiveSpeed(stack, state);
            if (speed <= 1.0f) continue; // this stack does not help mine the block

            if (priority.getValue() == Priority.Durability && !enderChest) {
                // Only tools that actually satisfy the harvest tier requirement count
                // (e.g. obsidian needs diamond+); everything else is filtered out.
                if (!stack.isCorrectToolForDrops(state)) continue;

                int tier = tierRank(stack);
                int remaining = stack.isDamageableItem()
                        ? stack.getMaxDamage() - stack.getDamageValue()
                        : Integer.MAX_VALUE;

                if (tier < bestTier || (tier == bestTier && remaining > bestRemaining)) {
                    bestTier = tier;
                    bestRemaining = remaining;
                    best = i;
                }
            } else {
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    best = i;
                }
            }
        }

        // Durability mode found nothing that can harvest -> fall back to fastest so we
        // still switch to a usable tool instead of mining bare-handed.
        if (best == -1 && priority.getValue() == Priority.Durability && !enderChest) {
            float fallbackSpeed = 1.0f;
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                if (saveItem.getValue() && stack.isDamageableItem()
                        && stack.getMaxDamage() - stack.getDamageValue() <= 10) {
                    continue;
                }
                float speed = effectiveSpeed(stack, state);
                if (speed > fallbackSpeed) {
                    fallbackSpeed = speed;
                    best = i;
                }
            }
        }

        return best;
    }

    private float effectiveSpeed(ItemStack stack, BlockState state) {
        float speed = stack.getDestroySpeed(state);
        int efficiency = EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
        if (efficiency > 0 && speed > 1.0f) {
            speed += efficiency * efficiency + 1;
        }
        return speed;
    }

    /**
     * Material tier ordering used by the durability-priority mode. Lower means cheaper,
     * so we wear those out first and keep the good gear intact. The actual harvest-tier
     * gate (e.g. obsidian needs diamond) is enforced separately via isCorrectToolForDrops.
     */
    private int tierRank(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.startsWith("wooden_")) return 0;
        if (path.startsWith("golden_")) return 1;
        if (path.startsWith("stone_")) return 2;
        if (path.startsWith("copper_")) return 3;
        if (path.startsWith("iron_")) return 4;
        if (path.startsWith("diamond_")) return 5;
        if (path.startsWith("netherite_")) return 6;
        return 3;
    }

}

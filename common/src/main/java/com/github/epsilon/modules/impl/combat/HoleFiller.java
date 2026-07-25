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
import com.github.epsilon.utils.world.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class HoleFiller extends Module {

    public static final HoleFiller INSTANCE = new HoleFiller();

    private HoleFiller() {
        super("Hole Filler", Category.COMBAT);
    }

    private enum FillBlock {
        Obsidian,
        Any
    }

    private final DoubleSetting range = doubleSetting("Range", 4.0, 2.0, 8.0, 0.5);
    private final BoolSetting onlyPlayers = boolSetting("Only Players", true);
    private final EnumSetting<FillBlock> fillBlock = enumSetting("Fill Block", FillBlock.Obsidian);

    private int tickCounter = 0;

    private static final Direction[] HORIZONTALS = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    @Override
    protected void onEnable() {
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;

        tickCounter++;
        if (tickCounter < 2) return;
        tickCounter = 0;

        FindItemResult result = findFillBlock();
        if (!result.found()) return;

        double r = range.getValue();
        AABB searchBox = mc.player.getBoundingBox().inflate(r);
        List<Entity> entities = mc.level.getEntities(mc.player, searchBox);

        for (Entity entity : entities) {
            if (onlyPlayers.getValue() && !(entity instanceof Player)) continue;
            if (entity.distanceTo(mc.player) > r) continue;

            BlockPos feet = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
            if (isInHole(feet)) {
                fillHole(feet, result.slot());
            }
        }
    }

    private boolean isInHole(BlockPos feet) {
        // Check that feet and feet+1 are air, and all 4 sides at feet level are solid
        if (!mc.level.getBlockState(feet).isAir()) return false;
        if (!mc.level.getBlockState(feet.above()).isAir()) return false;

        int solidCount = 0;
        for (Direction dir : HORIZONTALS) {
            BlockState state = mc.level.getBlockState(feet.relative(dir));
            if (!state.canBeReplaced() && !state.getCollisionShape(mc.level, feet.relative(dir)).isEmpty()) {
                solidCount++;
            }
        }
        return solidCount == 4;
    }

    private void fillHole(BlockPos holePos, int slot) {
        // Place blocks on the top edges of the hole entrance (adjacent solid blocks' top faces)
        for (Direction dir : HORIZONTALS) {
            BlockPos adjacent = holePos.relative(dir);
            BlockPos placeTarget = holePos.relative(dir.getOpposite());

            // Try placing on the adjacent solid block face
            BlockState adjState = mc.level.getBlockState(adjacent);
            if (!adjState.canBeReplaced() && !adjState.getCollisionShape(mc.level, adjacent).isEmpty()) {
                BlockPos above = holePos.above();
                if (BlockUtils.canPlaceAt(above)) {
                    Direction faceDir = Direction.DOWN;
                    BlockPos supportBlock = above.above();
                    BlockState support = mc.level.getBlockState(supportBlock);
                    // use the adjacent solid block as a placement surface
                    BlockHitResult hitResult = new BlockHitResult(
                            adjacent.getCenter().relative(Direction.WEST.getOpposite(), 0.0),
                            dir.getOpposite(),
                            adjacent,
                            false
                    );
                    InvUtils.swap(slot, true);
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                    InvUtils.swapBack();
                    return;
                }
            }
        }
    }

    private FindItemResult findFillBlock() {
        if (fillBlock.getValue() == FillBlock.Obsidian) {
            FindItemResult r = InvUtils.findInHotbar(Items.OBSIDIAN);
            if (r.found()) return r;
            return InvUtils.find(Items.OBSIDIAN);
        }
        // Any: find any block item
        FindItemResult r = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (r.found()) return r;
        r = InvUtils.find(Items.OBSIDIAN);
        if (r.found()) return r;
        return InvUtils.findInHotbar(stack -> {
            if (stack.isEmpty()) return false;
            return stack.getItem() instanceof net.minecraft.world.item.BlockItem;
        });
    }

}

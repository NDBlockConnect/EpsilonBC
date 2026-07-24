package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import com.github.epsilon.utils.rotation.RotationUtils;
import com.github.epsilon.utils.world.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SelfTrap extends Module {

    public static final SelfTrap INSTANCE = new SelfTrap();

    private SelfTrap() {
        super("Self Trap", Category.COMBAT);
    }

    private enum Mode {
        Normal,
        Strict
    }

    private final IntSetting height = intSetting("Height", 2, 1, 3, 1);
    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Normal);
    private final BoolSetting onlyOnGround = boolSetting("Only On Ground", false);

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (onlyOnGround.getValue() && !mc.player.onGround()) return;

        FindItemResult result = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (!result.found()) result = InvUtils.find(Items.OBSIDIAN);
        if (!result.found()) return;

        BlockPos head = BlockPos.containing(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(), mc.player.getZ());

        for (int y = 1; y <= height.getValue(); y++) {
            BlockPos target = head.above(y);
            if (!BlockUtils.canPlaceAt(target)) continue;

            Direction placeSide = getPlaceSide(target);
            if (placeSide == null) continue;

            BlockPos neighborBlock = target.relative(placeSide);
            Direction faceDir = placeSide.getOpposite();

            InvUtils.swap(result.slot(), true);
            BlockHitResult hitResult = new BlockHitResult(
                    neighborBlock.getCenter().relative(faceDir, 0.5),
                    faceDir,
                    neighborBlock,
                    false
            );
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
            InvUtils.swapBack();
        }
    }

    private Direction getPlaceSide(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState state = mc.level.getBlockState(neighbor);
            if (state.canBeReplaced()) continue;
            if (state.getCollisionShape(mc.level, neighbor).isEmpty()) continue;
            return dir;
        }
        return null;
    }

}

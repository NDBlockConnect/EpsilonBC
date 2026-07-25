package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.utils.player.FindItemResult;
import com.github.epsilon.utils.player.InvUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FacePlace extends Module {

    public static final FacePlace INSTANCE = new FacePlace();

    private FacePlace() {
        super("Face Place", Category.COMBAT);
    }

    private final DoubleSetting healthThreshold = doubleSetting("Health Threshold", 6.0, 1.0, 10.0, 0.5);
    private final BoolSetting armorThreshold = boolSetting("Armor Threshold", false);
    private final DoubleSetting armorThresholdValue = doubleSetting("Armor Threshold Value", 4.0, 0.0, 20.0, 0.5,
            armorThreshold::getValue);

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;

        Player target = findFacePlaceTarget();
        if (target == null) return;

        InteractionHand crystalHand = getCrystalHand();
        if (crystalHand == null) return;

        // Place crystal on the block above the target's head (face place position)
        BlockPos targetHeadPos = target.blockPosition().above(2);
        BlockPos support = targetHeadPos.below();

        // Support block must be obsidian or bedrock
        if (!mc.level.getBlockState(support).is(Blocks.OBSIDIAN)
                && !mc.level.getBlockState(support).is(Blocks.BEDROCK)) {
            // Try foot level support
            support = target.blockPosition();
            targetHeadPos = support.above();
            if (!mc.level.getBlockState(support).is(Blocks.OBSIDIAN)
                    && !mc.level.getBlockState(support).is(Blocks.BEDROCK)) {
                return;
            }
        }

        if (!mc.level.getBlockState(targetHeadPos).isAir()) return;

        AABB crystalBox = new AABB(
                targetHeadPos.getX(), targetHeadPos.getY(), targetHeadPos.getZ(),
                targetHeadPos.getX() + 1, targetHeadPos.getY() + 2, targetHeadPos.getZ() + 1
        );
        if (!mc.level.getEntities(null, crystalBox).isEmpty()) return;

        Vec3 hitVec = Vec3.atCenterOf(support).add(0.0, 0.5, 0.0);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, support, false);

        mc.gameMode.useItemOn(mc.player, crystalHand, hitResult);
        mc.player.swing(crystalHand);
    }

    private Player findFacePlaceTarget() {
        Player best = null;
        double bestDist = Double.MAX_VALUE;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;

            float health = player.getHealth() + player.getAbsorptionAmount();
            if (health > healthThreshold.getValue().floatValue()) continue;

            if (armorThreshold.getValue()) {
                float armor = player.getArmorValue();
                if (armor > armorThresholdValue.getValue().floatValue()) continue;
            }

            double dist = mc.player.distanceTo(player);
            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }

    private InteractionHand getCrystalHand() {
        if (mc.player.getMainHandItem().is(Items.END_CRYSTAL)) return InteractionHand.MAIN_HAND;
        if (mc.player.getOffhandItem().is(Items.END_CRYSTAL)) return InteractionHand.OFF_HAND;
        return null;
    }

}

package com.github.epsilon.utils.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.phys.AABB;

import static com.github.epsilon.Constants.mc;

public class BlockUtils {

    public static boolean canPlaceAt(BlockPos pos) {
        // 服务器会以"建筑高度上限：319"红字踢回任何越界放置，本地先挡掉，
        // 避免 CrystalAura/ZealotCrystalPlus/SelfTrap 等模块在 y>=world.getMaxY() 上疯狂发包。
        if (mc.level.isOutsideBuildHeight(pos)) return false;
        if (!mc.level.getBlockState(pos).canBeReplaced()) return false;
        return mc.level.getEntities((Entity) null, new AABB(pos), entity -> !(entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof ThrownExperienceBottle || entity instanceof Arrow)).isEmpty();
    }

}

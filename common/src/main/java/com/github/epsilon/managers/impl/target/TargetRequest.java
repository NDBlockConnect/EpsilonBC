package com.github.epsilon.managers.impl.target;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public record TargetRequest(
        double range,
        float fov,
        boolean player,
        boolean mob,
        boolean animal,
        boolean villager,
        boolean invisible,
        boolean passive,
        boolean teams,
        boolean named,
        Predicate<LivingEntity> extraFilter,
        int maxTargets
) {
    public TargetRequest {
        if (range < 0.0) range = 0.0;
        if (fov < 0.0f) fov = 0.0f;
        if (fov > 360.0f) fov = 360.0f;
        if (extraFilter == null) extraFilter = living -> true;
        if (maxTargets < 1) maxTargets = 1;
    }

    /** 兼容旧 8 参数（5 布尔）调用方。 */
    public static TargetRequest of(
            double range,
            float fov,
            boolean player,
            boolean mob,
            boolean animal,
            boolean villager,
            boolean invisible,
            int maxTargets
    ) {
        return new TargetRequest(range, fov, player, mob, animal, villager, invisible, false, false, false, living -> true, maxTargets);
    }

    /** 兼容旧 9 参数（5 布尔 + 谓词）调用方。 */
    public static TargetRequest of(
            double range,
            float fov,
            boolean player,
            boolean mob,
            boolean animal,
            boolean villager,
            boolean invisible,
            Predicate<LivingEntity> extraFilter,
            int maxTargets
    ) {
        return new TargetRequest(range, fov, player, mob, animal, villager, invisible, false, false, false, extraFilter, maxTargets);
    }

    /** Epsilon-Private 端口 11 参数（8 布尔）签名。 */
    public static TargetRequest of(
            double range,
            float fov,
            boolean player,
            boolean mob,
            boolean animal,
            boolean villager,
            boolean invisible,
            boolean passive,
            boolean teams,
            boolean named,
            int maxTargets
    ) {
        return new TargetRequest(range, fov, player, mob, animal, villager, invisible, passive, teams, named, living -> true, maxTargets);
    }
}

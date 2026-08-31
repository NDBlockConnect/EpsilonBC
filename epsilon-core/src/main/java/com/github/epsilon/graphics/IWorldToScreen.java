package com.github.epsilon.graphics.abstraction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 世界坐标转屏幕坐标工具接口，用于 ESP2D、NameTags。
 * <p>
 * 设计原则：
 * - 考虑摄像机插值（平滑渲染）
 * - 处理视锥体裁剪（返回 Optional）
 * - 支持实体与点坐标转换
 * <p>
 * 对应实现：
 * - OriginalLumin: {@code com.github.epsilon.graphics.world.WorldToScreen}
 * - OpenLumin: {@code io.github.openlumin.render.WorldToScreen}
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface IWorldToScreen {

    /**
     * 屏幕坐标结果（X, Y, Z）
     * <p>
     * Z 值：深度信息，用于排序（0.0 = 近裁剪面，1.0 = 远裁剪面）
     * 当 Z < 0 或 Z > 1 时，点在视锥体外
     */
    record ScreenPos(double x, double y, double z) {
        public boolean isOnScreen() {
            return z >= 0.0 && z <= 1.0;
        }
    }

    /**
     * 将世界坐标转换为屏幕坐标
     *
     * @param worldPos      世界空间坐标
     * @param partialTicks  帧内插值（0.0-1.0）
     * @return 屏幕坐标，若在视锥体外返回 empty
     */
    Optional<ScreenPos> toScreen(Vec3 worldPos, float partialTicks);

    /**
     * 将实体位置转换为屏幕坐标（自动插值）
     *
     * @param entity       目标实体
     * @param partialTicks 帧内插值
     * @return 屏幕坐标，若在视锥体外返回 empty
     */
    Optional<ScreenPos> toScreen(Entity entity, float partialTicks);

    /**
     * 获取插值后的世界坐标（摄像机平滑移动）
     *
     * @param entity       目标实体
     * @param partialTicks 帧内插值
     * @return 插值后的世界坐标
     */
    Vec3 interpolate(Entity entity, float partialTicks);

}

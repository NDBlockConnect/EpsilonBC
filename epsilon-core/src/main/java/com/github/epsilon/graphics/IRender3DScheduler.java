package com.github.epsilon.graphics.abstraction;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

/**
 * 3D 世界渲染调度器接口，用于 ESP、Tracers、世界几何渲染。
 * <p>
 * 设计原则：
 * - 声明式 API（描述"渲染什么"而非"如何渲染"）
 * - 自动合批优化（相同材质/拓扑的绘制合并）
 * - 多层排序支持（透明物体后绘制）
 * <p>
 * 对应实现：
 * - OriginalLumin: {@code com.github.epsilon.graphics.schedulers.render3d.Render3DScheduler}
 * - OpenLumin: {@code io.github.openlumin.render.Render3DScheduler}
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface IRender3DScheduler {

    /**
     * 添加填充盒渲染（半透明立方体，用于区域高亮）
     *
     * @param box   世界空间 AABB
     * @param color 填充颜色（支持 alpha）
     */
    void addFilledBox(AABB box, Color color);

    /**
     * 添加轮廓盒渲染（线框立方体，用于边界标记）
     *
     * @param box   世界空间 AABB
     * @param color 线条颜色
     */
    void addOutlineBox(AABB box, Color color);

    /**
     * 添加轮廓盒渲染（带线宽）
     *
     * @param box       世界空间 AABB
     * @param color     线条颜色
     * @param lineWidth 线宽（像素）
     */
    void addOutlineBox(AABB box, Color color, float lineWidth);

    /**
     * 添加线段渲染（用于 Tracers、连线）
     *
     * @param from  起点（世界坐标）
     * @param to    终点（世界坐标）
     * @param color 线条颜色
     */
    void addLine(Vec3 from, Vec3 to, Color color);

    /**
     * 添加线段渲染（带线宽）
     *
     * @param from      起点（世界坐标）
     * @param to        终点（世界坐标）
     * @param color     线条颜色
     * @param lineWidth 线宽（像素）
     */
    void addLine(Vec3 from, Vec3 to, Color color, float lineWidth);

    /**
     * 添加渐变线段渲染
     *
     * @param from       起点（世界坐标）
     * @param to         终点（世界坐标）
     * @param colorStart 起点颜色
     * @param colorEnd   终点颜色
     */
    void addGradientLine(Vec3 from, Vec3 to, Color colorStart, Color colorEnd);

    /**
     * 添加模糊盒渲染（背景模糊效果的立方体区域）
     *
     * @param box          世界空间 AABB
     * @param blurStrength 模糊强度（0.0 - 16.0）
     */
    void addBlurredBox(AABB box, double blurStrength);

    /**
     * 添加渐变填充盒渲染（底部到顶部颜色渐变）
     *
     * @param box         世界空间 AABB
     * @param bottomColor 底部颜色（ARGB 格式）
     * @param topColor    顶部颜色（ARGB 格式）
     */
    void addFilledFadeBox(AABB box, int bottomColor, int topColor);

    /**
     * 添加单面填充渲染（只渲染立方体的某一面）
     *
     * @param box       世界空间 AABB
     * @param color     填充颜色（ARGB 格式）
     * @param direction 要渲染的面
     */
    void addFilledSide(AABB box, int color, net.minecraft.core.Direction direction);

    /**
     * 添加单面轮廓渲染（只渲染立方体某一面的边框）
     *
     * @param box       世界空间 AABB
     * @param color     线条颜色（ARGB 格式）
     * @param thickness 线宽（像素）
     * @param direction 要渲染的面
     */
    void addSideOutline(AABB box, int color, float thickness, net.minecraft.core.Direction direction);

    /**
     * 清空当前帧的渲染队列（通常在帧结束时自动调用）
     */
    void clear();

}

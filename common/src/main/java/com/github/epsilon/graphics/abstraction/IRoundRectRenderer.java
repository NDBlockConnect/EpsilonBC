package com.github.epsilon.graphics.abstraction;

import java.awt.*;

/**
 * 圆角矩形渲染器接口，用于现代化 GUI 面板、按钮、卡片。
 * <p>
 * 设计原则：
 * - SDF 着色器实现（抗锯齿边缘）
 * - 支持多角独立圆角半径
 * - 支持渐变填充
 * <p>
 * 对应实现：
 * - OriginalLumin: {@code com.github.epsilon.graphics.round.RoundRectRenderer}
 * - OpenLumin: {@code io.github.openlumin.render.RoundRectRenderer}
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface IRoundRectRenderer {

    /**
     * 渲染圆角矩形（统一圆角半径）
     *
     * @param x      左上角 X 坐标
     * @param y      左上角 Y 坐标
     * @param width  宽度
     * @param height 高度
     * @param radius 圆角半径
     * @param color  填充颜色
     */
    void render(float x, float y, float width, float height, float radius, Color color);

    /**
     * 渲染圆角矩形（四角独立圆角半径）
     *
     * @param x           左上角 X 坐标
     * @param y           左上角 Y 坐标
     * @param width       宽度
     * @param height      高度
     * @param radiusTL    左上圆角半径
     * @param radiusTR    右上圆角半径
     * @param radiusBR    右下圆角半径
     * @param radiusBL    左下圆角半径
     * @param color       填充颜色
     */
    void render(float x, float y, float width, float height,
                float radiusTL, float radiusTR, float radiusBR, float radiusBL,
                Color color);

    /**
     * 渲染圆角矩形（垂直渐变）
     *
     * @param x      左上角 X 坐标
     * @param y      左上角 Y 坐标
     * @param width  宽度
     * @param height 高度
     * @param radius 圆角半径
     * @param colorTop    顶部颜色
     * @param colorBottom 底部颜色
     */
    void renderGradient(float x, float y, float width, float height, float radius,
                        Color colorTop, Color colorBottom);

}

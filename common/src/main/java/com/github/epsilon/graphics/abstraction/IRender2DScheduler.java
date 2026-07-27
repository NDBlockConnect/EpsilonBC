package com.github.epsilon.graphics.abstraction;

import java.awt.*;

/**
 * 2D GUI/HUD 渲染调度器接口，用于覆盖层、菜单、HUD 组件渲染。
 * <p>
 * 设计原则：
 * - 声明式 API（描述"渲染什么"而非"如何渲染"）
 * - 自动深度排序（Z-order 管理）
 * - 支持裁剪区域（嵌套面板剪裁）
 * <p>
 * 对应实现：
 * - OriginalLumin: {@code com.github.epsilon.graphics.schedulers.render2d.Render2DScheduler}
 * - OpenLumin: {@code io.github.openlumin.render.Render2DScheduler}
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface IRender2DScheduler {

    /**
     * 添加填充矩形渲染
     *
     * @param x     左上角 X 坐标（屏幕像素）
     * @param y     左上角 Y 坐标
     * @param width 宽度
     * @param height 高度
     * @param color 填充颜色
     */
    void addFilledRect(float x, float y, float width, float height, Color color);

    /**
     * 添加轮廓矩形渲染
     *
     * @param x     左上角 X 坐标
     * @param y     左上角 Y 坐标
     * @param width 宽度
     * @param height 高度
     * @param color 线条颜色
     */
    void addOutlineRect(float x, float y, float width, float height, Color color);

    /**
     * 添加渐变矩形渲染（垂直渐变）
     *
     * @param x      左上角 X 坐标
     * @param y      左上角 Y 坐标
     * @param width  宽度
     * @param height 高度
     * @param colorTop    顶部颜色
     * @param colorBottom 底部颜色
     */
    void addGradientRect(float x, float y, float width, float height, Color colorTop, Color colorBottom);

    /**
     * 清空当前帧的渲染队列
     */
    void clear();

}

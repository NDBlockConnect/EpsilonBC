package com.github.epsilon.graphics.abstraction;

import java.awt.*;

/**
 * 着色器效果管理器接口，用于模糊、发光、Chams。
 * <p>
 * 设计原则：
 * - 后处理效果（渲染到纹理，再应用着色器）
 * - 支持多层叠加（例如同时应用模糊和发光）
 * - 自动管理 FBO 和着色器生命周期
 * <p>
 * 对应实现：
 * - OriginalLumin: {@code com.github.epsilon.graphics.shaders.ShaderEffectManager}
 * - OpenLumin: {@code io.github.openlumin.render.ShaderEffectManager}
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface IShaderEffectManager {

    /**
     * 应用高斯模糊效果（用于背景模糊、面板模糊）
     *
     * @param x            区域左上角 X
     * @param y            区域左上角 Y
     * @param width        区域宽度
     * @param height       区域高度
     * @param radius       圆角半径
     * @param blurStrength 模糊强度（0.0-16.0）
     */
    void applyBlur(float x, float y, float width, float height, float radius, float blurStrength);

    /**
     * 应用发光效果（用于高亮、强调）
     *
     * @param x         区域左上角 X
     * @param y         区域左上角 Y
     * @param width     区域宽度
     * @param height    区域高度
     * @param glowColor 发光颜色
     * @param intensity 发光强度（0.0-1.0）
     */
    void applyGlow(float x, float y, float width, float height, Color glowColor, float intensity);

    /**
     * 开始 Chams 渲染（实体轮廓高亮）
     * <p>
     * 调用后，后续渲染的实体将应用 Chams 效果，
     * 必须调用 {@link #endChams()} 结束。
     *
     * @param color        Chams 颜色
     * @param throughWalls 是否穿墙可见
     */
    void beginChams(Color color, boolean throughWalls);

    /**
     * 结束 Chams 渲染
     */
    void endChams();

    /**
     * 应用自定义着色器（高级用途）
     *
     * @param shaderId 着色器资源 ID（例如 "epsilon:shaders/custom.fsh"）
     * @param x        区域左上角 X
     * @param y        区域左上角 Y
     * @param width    区域宽度
     * @param height   区域高度
     */
    void applyCustomShader(String shaderId, float x, float y, float width, float height);

}

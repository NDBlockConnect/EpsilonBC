package com.github.epsilon.graphics.abstraction;

import java.awt.*;

/**
 * 纹理渲染器接口，用于图标、图片、自定义 UV 纹理渲染。
 * <p>
 * 设计原则：
 * - 支持自定义 UV 坐标（纹理图集）
 * - 支持颜色叠加（着色/半透明）
 * - 支持旋转和缩放
 * <p>
 * 对应实现：
 * - OriginalLumin: {@code com.github.epsilon.graphics.texture.TextureRenderer}
 * - OpenLumin: {@code io.github.openlumin.render.TextureRenderer}
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface ITextureRenderer {

    /**
     * 渲染纹理（全纹理，无 UV 裁剪）
     *
     * @param textureId 纹理资源 ID（例如 "epsilon:textures/gui/icon.png"）
     * @param x         屏幕 X 坐标
     * @param y         屏幕 Y 坐标
     * @param width     渲染宽度
     * @param height    渲染高度
     */
    void render(String textureId, float x, float y, float width, float height);

    /**
     * 渲染纹理（带颜色叠加）
     *
     * @param textureId 纹理资源 ID
     * @param x         屏幕 X 坐标
     * @param y         屏幕 Y 坐标
     * @param width     渲染宽度
     * @param height    渲染高度
     * @param color     叠加颜色（支持 alpha）
     */
    void render(String textureId, float x, float y, float width, float height, Color color);

    /**
     * 渲染纹理（自定义 UV 坐标）
     *
     * @param textureId 纹理资源 ID
     * @param x         屏幕 X 坐标
     * @param y         屏幕 Y 坐标
     * @param width     渲染宽度
     * @param height    渲染高度
     * @param u0        纹理 U 起始坐标（0.0-1.0）
     * @param v0        纹理 V 起始坐标（0.0-1.0）
     * @param u1        纹理 U 结束坐标（0.0-1.0）
     * @param v1        纹理 V 结束坐标（0.0-1.0）
     * @param color     叠加颜色
     */
    void renderUV(String textureId, float x, float y, float width, float height,
                  float u0, float v0, float u1, float v1, Color color);

    /**
     * 渲染旋转纹理
     *
     * @param textureId 纹理资源 ID
     * @param x         屏幕 X 坐标
     * @param y         屏幕 Y 坐标
     * @param width     渲染宽度
     * @param height    渲染高度
     * @param rotation  旋转角度（弧度）
     * @param color     叠加颜色
     */
    void renderRotated(String textureId, float x, float y, float width, float height,
                       float rotation, Color color);

}

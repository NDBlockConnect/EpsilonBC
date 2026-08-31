package com.github.epsilon.graphics.abstraction;

/**
 * 图形后端适配器接口，统一 OriginalLumin 和 OpenLumin API。
 * <p>
 * 设计原则：
 * - 零开销抽象（接口方法内联）
 * - 保持与 OpenLumin API 接口兼容
 * - 支持运行时后端切换
 * <p>
 * 实现：
 * - OriginalLuminAdapter: 包装当前自研图形库（Alpha 3-5）
 * - OpenLuminAdapter: 适配 OpenLumin v26.0+（Alpha 5+）
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public interface IGraphicsAdapter {

    /**
     * 获取 2D 渲染调度器（用于 GUI、HUD、覆盖层渲染）
     */
    IRender2DScheduler getRender2DScheduler();

    /**
     * 获取 3D 世界渲染调度器（用于 ESP、Tracers、世界几何渲染）
     */
    IRender3DScheduler getRender3DScheduler();

    /**
     * 获取圆角矩形渲染器（用于 GUI 面板、按钮、HUD 组件）
     */
    IRoundRectRenderer getRoundRectRenderer();

    /**
     * 获取纹理渲染器（用于图标、图片、自定义 UV 纹理）
     */
    ITextureRenderer getTextureRenderer();

    /**
     * 获取世界坐标转屏幕坐标工具（用于 ESP2D、NameTags）
     */
    IWorldToScreen getWorldToScreen();

    /**
     * 获取着色器效果管理器（用于模糊、发光、Chams）
     */
    IShaderEffectManager getShaderEffectManager();

    /**
     * 获取当前图形后端名称（"OriginalLumin" 或 "OpenLumin"）
     */
    String getBackendName();

    /**
     * 获取后端版本（例如 "v26.0-alpha.3"）
     */
    String getBackendVersion();

}

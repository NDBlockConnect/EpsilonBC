package com.github.epsilon.platform;

import com.github.epsilon.gui.lib.UiTextMetrics;
import com.github.epsilon.gui.lib.UiTheme;
import com.github.epsilon.gui.lib.UiTree;
import com.github.epsilon.gui.lib.scene.UiLayer;
import com.github.epsilon.gui.lib.scene.UiScene;

import java.nio.file.Path;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

/**
 * EpsilonShot UI 运行时抽象。
 *
 * <p>上游 Epsilon 26.1.x 的新 GUI 直接依赖外部 MinecraftUiRuntime2612；
 * 本抽象以同形 API 覆盖其消费面，使 Screen 层代码可在不同图形后端之间移植：
 * InternalUiRuntime（内建 gui.lib, 26.2 Vulkan 管线）为首个实现；
 * 预留 OpenLuminRuntime / LuminGraphicsRuntime。</p>
 *
 * <p>线程约定：所有方法仅在渲染线程调用。</p>
 */
public interface UiRuntime extends AutoCloseable {

    /** 当前帧活跃的运行时；无活跃实现时返回内建实现。 */
    static UiRuntime current() {
        return UiRuntimeRegistry.current();
    }

    /** 同 current()，但无活跃实现时返回 null（上游 currentOrNull 语义）。 */
    static UiRuntime currentOrNull() {
        return UiRuntimeRegistry.currentOrNull();
    }

    /** 运行时标识（日志与缓存键）。 */
    String id();

    /** 创建挂接本运行时的场景。 */
    UiScene createScene(UiTheme theme);

    /** Screen 侧帧提交：回调内通过 scene.batch/submit 组装，返回后统一呈现。 */
    void render(UiScene scene, Consumer<UiScene> frame);

    /** 模块侧独立树提交（HUD/Level 通道）。 */
    void render(UiScene scene, UiLayer layer, UiTree tree);

    /** 文本度量（字体名由 BuiltInTextMetrics 解析）。 */
    UiTextMetrics textMetrics();

    // ── 字体注册与应用（上游 configureMinecraftFonts 消费面） ──

    /** 注册命名字体；重复注册同名为幂等操作。 */
    void registerFont(String name, Identifier fontFile);

    /** 将默认字体切换为已注册的命名字体。 */
    void useDefaultFont(String name);

    /** 注册并启用自定义 TTF（路径来源），失败时抛出 RuntimeException 由调用方回退。 */
    void useCustomDefaultFont(String name, Path fontPath);

    /** 强制同步解析命名字体（上游 font(name) 语义：立即加载验证）。 */
    void font(String name);

    /** 每帧字形上传预算。 */
    void setFontGlyphsPerFrame(int maxGlyphsPerFrame);

    /** UI 文本缩放倍率。 */
    void setUiTextScaleMultiplier(float multiplier);

    /** 投影缩放（GUI scale 对齐）。 */
    void setProjectionScale(float scale);

    // ── 生命周期 ──

    /** 场景声明期守卫：声明窗口外调用声明类 API 时抛出 IllegalStateException。 */
    void requireDeclarationsOpen();

    /** 关闭声明窗口。 */
    void closeDeclarations();

    /** 运行时存活守卫（上游 requireAlive 语义：close 后调用抛出）。 */
    void requireAlive();

    @Override
    void close();
}

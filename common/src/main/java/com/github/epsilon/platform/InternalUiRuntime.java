package com.github.epsilon.platform;

import com.github.epsilon.Constants;
import com.github.epsilon.gui.lib.BuiltInTextMetrics;
import com.github.epsilon.gui.lib.UiTextMetrics;
import com.github.epsilon.gui.lib.UiTheme;
import com.github.epsilon.gui.lib.UiTree;
import com.github.epsilon.gui.lib.scene.UiLayer;
import com.github.epsilon.gui.lib.scene.UiScene;
import com.github.epsilon.graphics.text.StaticFontLoader;
import com.github.epsilon.graphics.text.ttf.TtfFontLoader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

/**
 * {@link UiRuntime} 的内建实现：直接驱动 gui.lib 声明式树与
 * EpsilonBC 自有渲染管线（26.2 Vulkan/OpenGL 路径）。
 *
 * <p>帧语义：{@link #render(UiScene, Consumer)} = beginFrame → frame → endFrame；
 * {@link #render(UiScene, UiLayer, UiTree)} 同理，以单树提交 CONTENT 层。</p>
 */
public final class InternalUiRuntime implements UiRuntime {

    private static final InternalUiRuntime INSTANCE = new InternalUiRuntime();

    private final Map<String, TtfFontLoader> fonts = new HashMap<>();
    private boolean alive = true;
    private boolean declarationsOpen;
    private int fontGlyphsPerFrame = 8;
    private float uiTextScaleMultiplier = 1.0f;
    private float projectionScale = 2.0f;

    private InternalUiRuntime() {
        fonts.put("epsilon-default", StaticFontLoader.DEFAULT);
        fonts.put("epsilon-icons", StaticFontLoader.ICONS);
        fonts.put("epsilon-jura-light", StaticFontLoader.JURA_LIGHT);
        fonts.put("epsilon-osakachips", StaticFontLoader.OSAKA_CHIPS);
    }

    /** 内建单例（UiRuntimeRegistry 的回退目标）。 */
    public static InternalUiRuntime get() {
        return INSTANCE;
    }

    @Override
    public String id() {
        return "epsilon-internal";
    }

    @Override
    public UiScene createScene(UiTheme theme) {
        requireAlive();
        return new UiScene(theme);
    }

    @Override
    public void render(UiScene scene, Consumer<UiScene> frame) {
        requireAlive();
        scene.beginFrame();
        try {
            frame.accept(scene);
        } finally {
            scene.endFrame();
        }
    }

    @Override
    public void render(UiScene scene, UiLayer layer, UiTree tree) {
        requireAlive();
        if (tree.nodeCount() == 0) return;
        scene.beginFrame();
        try {
            scene.submit(layer, 0, tree);
        } finally {
            scene.endFrame();
        }
    }

    @Override
    public UiTextMetrics textMetrics() {
        return BuiltInTextMetrics.get();
    }

    @Override
    public void registerFont(String name, Identifier fontFile) {
        fonts.computeIfAbsent(name, key -> new TtfFontLoader(fontFile));
    }

    @Override
    public void useDefaultFont(String name) {
        TtfFontLoader loader = fonts.get(name);
        if (loader == null) {
            Constants.LOGGER.warn("UiRuntime: unknown font '{}', keeping current default", name);
            return;
        }
        StaticFontLoader.DEFAULT = loader;
    }

    @Override
    public void useCustomDefaultFont(String name, Path fontPath) {
        TtfFontLoader loader = fonts.computeIfAbsent(name, key -> new TtfFontLoader(fontPath));
        StaticFontLoader.DEFAULT = loader;
    }

    @Override
    public void font(String name) {
        // 内建 TtfFontLoader 惰性加载；此处通过度量一次强制解析以验证字体可用。
        TtfFontLoader loader = fonts.get(name);
        if (loader == null) {
            throw new IllegalStateException("UiRuntime: font not registered: " + name);
        }
        BuiltInTextMetrics.get().textHeight(1.0f, loader);
    }

    @Override
    public void setFontGlyphsPerFrame(int maxGlyphsPerFrame) {
        this.fontGlyphsPerFrame = Math.max(1, maxGlyphsPerFrame);
    }

    @Override
    public void setUiTextScaleMultiplier(float multiplier) {
        this.uiTextScaleMultiplier = multiplier;
    }

    @Override
    public void setProjectionScale(float scale) {
        // 内建树直接工作在 MC GUI 坐标系，投影缩放由原版 guiScale 处理；
        // 保留数值供未来需要独立投影时使用。
        this.projectionScale = scale;
    }

    @Override
    public void requireDeclarationsOpen() {
        if (!declarationsOpen) {
            throw new IllegalStateException("UiRuntime: declaration window is not open");
        }
    }

    @Override
    public void closeDeclarations() {
        declarationsOpen = false;
    }

    @Override
    public void requireAlive() {
        if (!alive) {
            throw new IllegalStateException("UiRuntime '" + id() + "' is closed");
        }
    }

    // 内建实现为进程级单例；close 仅标记死亡以便生命周期测试，正常流程不调用。
    @Override
    public void close() {
        alive = false;
    }

    /** 测试与重初始化辅助：恢复存活并重开声明窗口。 */
    void revive() {
        alive = true;
    }
}

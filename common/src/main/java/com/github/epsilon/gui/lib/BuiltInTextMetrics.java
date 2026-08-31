package com.github.epsilon.gui.lib;

import com.github.epsilon.graphics.renderers.TextRenderer;
import com.github.epsilon.graphics.text.StaticFontLoader;
import com.github.epsilon.graphics.text.ttf.TtfFontLoader;

/**
 * 内建 {@link UiTextMetrics} 实现。
 * <p>
 * 上游 Lumin Graphics 的文本度量接受字体名字符串；为平滑迁移，
 * 此处在内建 {@link TextRenderer} 之上提供等价能力与字体名解析。
 */
public final class BuiltInTextMetrics implements UiTextMetrics {

    private static final class H {
        private static final BuiltInTextMetrics INSTANCE = new BuiltInTextMetrics();
    }

    private final TextRenderer renderer = TextRenderer.create();

    private BuiltInTextMetrics() {
    }

    public static BuiltInTextMetrics get() {
        return H.INSTANCE;
    }

    /**
     * 将上游字体名映射为内建静态字体；未知或空名称回退到默认字体。
     */
    public static TtfFontLoader resolveFont(String font) {
        if (font == null || font.isBlank() || "default".equals(font) || "epsilon-default".equals(font)) {
            return null;
        }
        return switch (font.toLowerCase().replace('-', '_')) {
            case "icons", "epsilon_icons" -> StaticFontLoader.ICONS;
            case "jura", "jura_light", "epsilon_jura" -> StaticFontLoader.JURA_LIGHT;
            case "osaka", "osaka_chips", "epsilon_osaka" -> StaticFontLoader.OSAKA_CHIPS;
            default -> null;
        };
    }

    public float textWidth(String text, float scale, String fontName) {
        return renderer.getWidth(text, scale, resolveFont(fontName));
    }

    public float textHeight(float scale, String fontName) {
        return renderer.getHeight(scale, resolveFont(fontName));
    }

    @Override
    public float textWidth(String text, float scale) {
        return renderer.getWidth(text, scale);
    }

    @Override
    public float textWidth(String text, float scale, TtfFontLoader fontLoader) {
        return renderer.getWidth(text, scale, fontLoader);
    }

    @Override
    public float textHeight(float scale) {
        return renderer.getHeight(scale);
    }

    @Override
    public float textHeight(float scale, TtfFontLoader fontLoader) {
        return renderer.getHeight(scale, fontLoader);
    }
}

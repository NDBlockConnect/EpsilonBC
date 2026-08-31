package com.github.epsilon.gui.lib;

import java.awt.Color;

/**
 * 上游 Lumin Graphics LuminColorShim 的内建等价 shim。
 *
 * <p>上游用 LuminColorShim 包装 ARGB 与颜色运算；内建树直接消费
 * {@link Color}，此处仅提供消费面出现过的静态工厂/转换。</p>
 */
public final class LuminColorShim {

    private LuminColorShim() {
    }

    /** ARGB 整数 → Color（上游 LuminColorShim.of(int) 语义）。 */
    public static Color of(int argb) {
        return new Color(argb, true);
    }

    /** 通道分量 → Color（上游 LuminColorShim.of(r,g,b,a) 语义，0..255）。 */
    public static Color of(int r, int g, int b, int a) {
        return new Color(clamp(r), clamp(g), clamp(b), clamp(a));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}

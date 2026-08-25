package com.github.epsilon.scripting.lua.render;

import com.github.epsilon.gui.lib.UiRect;
import com.github.epsilon.gui.lib.UiTextMetrics;
import com.github.epsilon.gui.lib.UiTree;
import com.github.epsilon.graphics.renderers.TextRenderer;
import com.github.epsilon.graphics.text.ttf.TtfFontLoader;
import com.github.epsilon.scripting.lua.LuaRuntime;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.awt.Color;

/**
 * Lua UI 绘制上下文。
 * <p>
 * 上游实现绑定外部 Lumin Graphics 运行时；此处适配为内建
 * {@code com.github.epsilon.gui.lib} 声明式 UI 树，API 语义保持一致。
 */
public final class LuaUiContext {
    private LuaUiContext() {
    }

    private static final class Metrics implements UiTextMetrics {
        private final TextRenderer renderer = TextRenderer.create();

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

    private static final class MetricsHolder {
        private static final Metrics INSTANCE = new Metrics();
    }

    private static UiTextMetrics metrics() {
        return MetricsHolder.INSTANCE;
    }

    /**
     * 将上游字体名映射为内建静态字体；未知名称回退到默认字体。
     */
    private static TtfFontLoader resolveFont(String font) {
        if (font == null || font.isBlank() || "default".equals(font)) {
            return null;
        }
        return switch (font.toLowerCase().replace('-', '_')) {
            case "icons" -> com.github.epsilon.graphics.text.StaticFontLoader.ICONS;
            case "jura", "jura_light" -> com.github.epsilon.graphics.text.StaticFontLoader.JURA_LIGHT;
            case "osaka", "osaka_chips" -> com.github.epsilon.graphics.text.StaticFontLoader.OSAKA_CHIPS;
            default -> null;
        };
    }

    public static LuaTable create(LuaRuntime runtime, UiTree.Scope scope) {
        LuaTable api = new LuaTable();
        api.set("rect", function(args -> {
            scope.rect(number(args, 2), number(args, 3), number(args, 4), number(args, 5), color(args.arg(6)));
            return LuaValue.NONE;
        }));
        api.set("round_rect", function(args -> {
            scope.roundRect(number(args, 2), number(args, 3), number(args, 4), number(args, 5),
                    number(args, 6), color(args.arg(7)));
            return LuaValue.NONE;
        }));
        api.set("outline", function(args -> {
            scope.outline(number(args, 2), number(args, 3), number(args, 4), number(args, 5),
                    number(args, 6), number(args, 7), color(args.arg(8)));
            return LuaValue.NONE;
        }));
        api.set("shadow", function(args -> {
            scope.shadow(number(args, 2), number(args, 3), number(args, 4), number(args, 5),
                    number(args, 6), number(args, 7), color(args.arg(8)));
            return LuaValue.NONE;
        }));
        api.set("text", function(args -> {
            String font = optionalString(args.arg(7));
            scope.text(args.arg(2).checkjstring(), number(args, 3), number(args, 4), number(args, 5),
                    color(args.arg(6)), resolveFont(font));
            return LuaValue.NONE;
        }));
        api.set("rotated_text", function(args -> {
            String font = optionalString(args.arg(7));
            scope.rotatedText(args.arg(2).checkjstring(), number(args, 3), number(args, 4), number(args, 5),
                    color(args.arg(6)), resolveFont(font),
                    number(args, 8), number(args, 9), number(args, 10));
            return LuaValue.NONE;
        }));
        api.set("texture", function(args -> {
            scope.texture(identifier(args.arg(2).checkjstring()), number(args, 3), number(args, 4),
                    number(args, 5), number(args, 6), color(args.arg(7)));
            return LuaValue.NONE;
        }));
        api.set("triangle", function(args -> {
            // 上游签名 triangle(x, y, w, h, color)；内建树使用 (centerX, centerY, size, progress)。
            float x = number(args, 2);
            float y = number(args, 3);
            float w = number(args, 4);
            float h = number(args, 5);
            scope.triangle(x + w / 2.0f, y + h / 2.0f, Math.max(w, h), 1.0f, color(args.arg(6)));
            return LuaValue.NONE;
        }));
        api.set("layer", nested(runtime, scope, NestedKind.LAYER));
        api.set("scissor", nested(runtime, scope, NestedKind.SCISSOR));
        api.set("push_absolute", nested(runtime, scope, NestedKind.ABSOLUTE));
        api.set("text_width", function(args -> LuaValue.valueOf(metrics()
                .textWidth(args.arg(2).checkjstring(), number(args, 3),
                        resolveFont(optionalString(args.arg(4)))))));
        api.set("text_height", function(args -> LuaValue.valueOf(metrics()
                .textHeight(number(args, 2), resolveFont(optionalString(args.arg(3)))))));
        api.set("raw_scope", function(args -> CoerceJavaToLua.coerce(scope)));
        return api;
    }

    private static VarArgFunction nested(LuaRuntime runtime, UiTree.Scope scope, NestedKind kind) {
        return function(args -> {
            LuaValue callback;
            switch (kind) {
                case LAYER -> {
                    int layer = exactInt(args.arg(2), "layer");
                    callback = args.arg(3).checkfunction();
                    scope.layer(layer, child -> runtime.invoke(callback, create(runtime, child)));
                }
                case SCISSOR -> {
                    callback = args.arg(6).checkfunction();
                    UiRect rect = new UiRect(number(args, 2), number(args, 3), number(args, 4), number(args, 5));
                    scope.scissor(rect, child -> runtime.invoke(callback, create(runtime, child)));
                }
                case ABSOLUTE -> {
                    callback = args.arg(6).checkfunction();
                    UiRect rect = new UiRect(number(args, 2), number(args, 3), number(args, 4), number(args, 5));
                    scope.pushAbsolute(rect, child -> runtime.invoke(callback, create(runtime, child)));
                }
            }
            return LuaValue.NONE;
        });
    }

    private static net.minecraft.resources.Identifier identifier(String value) {
        if (value.indexOf(':') >= 0) {
            String[] parts = value.split(":", 2);
            return net.minecraft.resources.Identifier.fromNamespaceAndPath(parts[0], parts[1]);
        }
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("epsilon", value);
    }

    private static float number(Varargs args, int index) {
        return (float) finite(args.arg(index), "参数 #" + (index - 1));
    }

    private static Color color(LuaValue value) {
        double number = finite(value, "color");
        if (number != Math.rint(number) || number < Integer.MIN_VALUE || number > 0xFFFF_FFFFL) {
            throw new LuaError("color 必须是 32-bit ARGB 整数");
        }
        return new Color((int) (long) number, true);
    }

    private static String optionalString(LuaValue value) {
        return value.isnil() ? null : value.checkjstring();
    }

    private static int exactInt(LuaValue value, String name) {
        double number = finite(value, name);
        if (number != Math.rint(number) || number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw new LuaError(name + " 必须是 32-bit 整数");
        }
        return (int) number;
    }

    private static double finite(LuaValue value, String name) {
        double number = value.checkdouble();
        if (!Double.isFinite(number)) throw new LuaError(name + " 必须是有限 number");
        return number;
    }

    private static VarArgFunction function(java.util.function.Function<Varargs, LuaValue> body) {
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return body.apply(args);
            }
        };
    }

    private enum NestedKind {
        LAYER, SCISSOR, ABSOLUTE
    }
}

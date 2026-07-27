package com.github.epsilon.graphics.abstraction;

import com.github.epsilon.graphics.renderers.RoundRectRenderer;
import com.github.epsilon.graphics.renderers.TextureRenderer;
import com.github.epsilon.graphics.schedulers.render2d.Render2DScheduler;
import com.github.epsilon.graphics.schedulers.render3d.Render3DScheduler;
import com.github.epsilon.graphics.shaders.BlurShader;
import com.github.epsilon.utils.render.WorldToScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4d;

import java.awt.*;
import java.util.Optional;

/**
 * OriginalLumin 图形后端适配器（Alpha 3-5）
 * <p>
 * 包装当前自研图形库，提供统一接口。
 * Alpha 5 后将被 OpenLuminAdapter 替代。
 *
 * @author EpsilonBC Team
 * @since Alpha 3
 */
public class OriginalLuminAdapter implements IGraphicsAdapter {

    private static final OriginalLuminAdapter INSTANCE = new OriginalLuminAdapter();

    private final Render2DSchedulerImpl render2DScheduler = new Render2DSchedulerImpl();
    private final Render3DSchedulerImpl render3DScheduler = new Render3DSchedulerImpl();
    private final RoundRectRendererImpl roundRectRenderer = new RoundRectRendererImpl();
    private final TextureRendererImpl textureRenderer = new TextureRendererImpl();
    private final WorldToScreenImpl worldToScreen = new WorldToScreenImpl();
    private final ShaderEffectManagerImpl shaderEffectManager = new ShaderEffectManagerImpl();

    private OriginalLuminAdapter() {
    }

    public static OriginalLuminAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public IRender2DScheduler getRender2DScheduler() {
        return render2DScheduler;
    }

    @Override
    public IRender3DScheduler getRender3DScheduler() {
        return render3DScheduler;
    }

    @Override
    public IRoundRectRenderer getRoundRectRenderer() {
        return roundRectRenderer;
    }

    @Override
    public ITextureRenderer getTextureRenderer() {
        return textureRenderer;
    }

    @Override
    public IWorldToScreen getWorldToScreen() {
        return worldToScreen;
    }

    @Override
    public IShaderEffectManager getShaderEffectManager() {
        return shaderEffectManager;
    }

    @Override
    public String getBackendName() {
        return "OriginalLumin";
    }

    @Override
    public String getBackendVersion() {
        return "v26.0-alpha.3";
    }

    // ==================== 内部实现类 ====================

    private static class Render2DSchedulerImpl implements IRender2DScheduler {
        @Override
        public void addFilledRect(float x, float y, float width, float height, Color color) {
            // OriginalLumin 的 Render2DScheduler 目前没有直接的矩形 API
            // 使用 RoundRectRenderer 以 radius=0 模拟
            RoundRectRenderer renderer = RoundRectRenderer.create();
            renderer.addRoundRect(x, y, width, height, 0f, color);
            renderer.drawAndClear();
        }

        @Override
        public void addOutlineRect(float x, float y, float width, float height, Color color) {
            // 暂不支持，需要后续实现或使用 RoundRectOutlineRenderer
            // 占位实现，避免编译错误
        }

        @Override
        public void addGradientRect(float x, float y, float width, float height, Color colorTop, Color colorBottom) {
            RoundRectRenderer renderer = RoundRectRenderer.create();
            renderer.addVerticalGradient(x, y, width, height, 0f, colorTop, colorBottom);
            renderer.drawAndClear();
        }

        @Override
        public void clear() {
            // Render2DScheduler 无需显式清空（每帧自动管理）
        }
    }

    private static class Render3DSchedulerImpl implements IRender3DScheduler {
        @Override
        public void addFilledBox(AABB box, Color color) {
            Render3DScheduler.INSTANCE.addFilledBox(box, color);
        }

        @Override
        public void addOutlineBox(AABB box, Color color) {
            Render3DScheduler.INSTANCE.addOutlineBox(box, color);
        }

        @Override
        public void addOutlineBox(AABB box, Color color, float lineWidth) {
            Render3DScheduler.INSTANCE.addOutlineBox(box, color.getRGB(), lineWidth);
        }

        @Override
        public void addLine(Vec3 from, Vec3 to, Color color) {
            Render3DScheduler.INSTANCE.addLine(from, to, color, 2.0f);
        }

        @Override
        public void addLine(Vec3 from, Vec3 to, Color color, float lineWidth) {
            Render3DScheduler.INSTANCE.addLine(from, to, color, lineWidth);
        }

        @Override
        public void addGradientLine(Vec3 from, Vec3 to, Color colorStart, Color colorEnd) {
            // OriginalLumin 暂不支持渐变线，回退到单色
            addLine(from, to, colorStart, 2.0f);
        }

        @Override
        public void clear() {
            Render3DScheduler.INSTANCE.clear();
        }
    }

    private static class RoundRectRendererImpl implements IRoundRectRenderer {
        @Override
        public void render(float x, float y, float width, float height, float radius, Color color) {
            RoundRectRenderer renderer = RoundRectRenderer.create();
            renderer.addRoundRect(x, y, width, height, radius, color);
            renderer.drawAndClear();
        }

        @Override
        public void render(float x, float y, float width, float height, float radiusTL, float radiusTR, float radiusBR, float radiusBL, Color color) {
            RoundRectRenderer renderer = RoundRectRenderer.create();
            renderer.addRoundRect(x, y, width, height, radiusTL, radiusTR, radiusBR, radiusBL, color);
            renderer.drawAndClear();
        }

        @Override
        public void renderGradient(float x, float y, float width, float height, float radius, Color colorTop, Color colorBottom) {
            RoundRectRenderer renderer = RoundRectRenderer.create();
            renderer.addVerticalGradient(x, y, width, height, radius, colorTop, colorBottom);
            renderer.drawAndClear();
        }
    }

    private static class TextureRendererImpl implements ITextureRenderer {
        @Override
        public void render(String textureId, float x, float y, float width, float height) {
            render(textureId, x, y, width, height, Color.WHITE);
        }

        @Override
        public void render(String textureId, float x, float y, float width, float height, Color color) {
            TextureRenderer renderer = TextureRenderer.create();
            renderer.addQuadTexture(Identifier.parse(textureId), x, y, width, height, 0f, 0f, 1f, 1f, color);
            renderer.drawAndClear();
        }

        @Override
        public void renderUV(String textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1, Color color) {
            TextureRenderer renderer = TextureRenderer.create();
            renderer.addQuadTexture(Identifier.parse(textureId), x, y, width, height, u0, v0, u1, v1, color);
            renderer.drawAndClear();
        }

        @Override
        public void renderRotated(String textureId, float x, float y, float width, float height, float rotation, Color color) {
            // OriginalLumin TextureRenderer 暂不直接支持旋转，占位实现
            render(textureId, x, y, width, height, color);
        }
    }

    private static class WorldToScreenImpl implements IWorldToScreen {
        @Override
        public Optional<ScreenPos> toScreen(Vec3 worldPos, float partialTicks) {
            Vector3f screen = WorldToScreen.getWorldPositionToScreen(worldPos);
            if (screen.z < 0.0f || screen.z > 1.0f) {
                return Optional.empty();
            }
            return Optional.of(new ScreenPos(screen.x, screen.y, screen.z));
        }

        @Override
        public Optional<ScreenPos> toScreen(Entity entity, float partialTicks) {
            Vec3 interpolated = interpolate(entity, partialTicks);
            return toScreen(interpolated, partialTicks);
        }

        @Override
        public Vec3 interpolate(Entity entity, float partialTicks) {
            return WorldToScreen.interpolate(entity, partialTicks);
        }
    }

    private static class ShaderEffectManagerImpl implements IShaderEffectManager {
        @Override
        public void applyBlur(float x, float y, float width, float height, float radius) {
            BlurShader.INSTANCE.render(x, y, width, height, 0f, 0f, 0f, 0f, radius);
        }

        @Override
        public void applyGlow(float x, float y, float width, float height, Color glowColor, float intensity) {
            // OriginalLumin 暂无独立 Glow 实现，占位
        }

        @Override
        public void beginChams(Color color, boolean throughWalls) {
            // Chams 由各模块独立实现（CrystalChams 等），此处占位
        }

        @Override
        public void endChams() {
            // 占位
        }

        @Override
        public void applyCustomShader(String shaderId, float x, float y, float width, float height) {
            // 自定义着色器暂不支持，占位
        }
    }
}

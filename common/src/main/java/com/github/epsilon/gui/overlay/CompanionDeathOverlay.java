package com.github.epsilon.gui.overlay;

import com.github.epsilon.assets.resources.ResourceLocationUtils;
import com.github.epsilon.events.bus.EventBus;
import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.GameLeftEvent;
import com.github.epsilon.events.impl.PacketEvent;
import com.github.epsilon.events.impl.Render2DEvent;
import com.github.epsilon.graphics.LuminRenderSystem;
import com.github.epsilon.gui.lib.UiTree;
import com.github.epsilon.gui.lib.scene.UiLayer;
import com.github.epsilon.gui.lib.scene.UiScene;
import com.github.epsilon.gui.theme.EpsilonUiTheme;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.impl.ClientSetting;
import com.github.epsilon.utils.render.animation.Easing;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.awt.*;

import static com.github.epsilon.Constants.mc;

/**
 * 伴侣角色死亡弹出覆盖层。
 * 当本地玩家死亡时，在 HUD 右侧滑入角色立绘并播放死亡语音，
 * 由 {@link ClientSetting#showCompanionOnDeath} 控制开关。
 * <p>
 * 渲染方案与 MainMenuScreen 相同：独立 LuminRenderTarget + UiTree，
 * 支持 alpha 通道，避开已被移除的 RenderSystem.setShaderColor。
 */
public class CompanionDeathOverlay {

    public static final CompanionDeathOverlay INSTANCE = new CompanionDeathOverlay();

    /** 角色立绘宽高比（710 × 1280）*/
    private static final float ASPECT_RATIO = 710.0f / 1280.0f;

    private static final long APPEAR_DURATION_MS = 400L;
    private static final long HOLD_DURATION_MS   = 4_000L;
    private static final long FADE_START_MS      = APPEAR_DURATION_MS + HOLD_DURATION_MS;
    private static final long FADE_DURATION_MS   = 600L;
    private static final long TOTAL_DURATION_MS  = FADE_START_MS + FADE_DURATION_MS;

    /** 死亡动画开始时间戳，-1 表示未激活 */
    private long deathStartMs = -1L;

    private LuminRenderSystem.LuminRenderTarget renderTarget;
    private final UiScene scene = new UiScene(EpsilonUiTheme.INSTANCE);

    private CompanionDeathOverlay() {}

    /** 在 EpsilonCommon.init() 中调用，注册事件订阅 */
    public static void init() {
        EventBus.INSTANCE.subscribe(INSTANCE);
    }

    // ── 事件处理 ─────────────────────────────────────────────────────────────

    /**
     * 在 netty 线程收到。仅识别本地玩家死亡包，defer 到主线程触发动画+语音。
     */
    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundPlayerCombatKillPacket packet)) return;
        if (mc.player == null || packet.playerId() != mc.player.getId()) return;
        if (!ClientSetting.INSTANCE.showCompanionOnDeath.getValue()) return;

        mc.execute(() -> {
            if (mc.player == null) return;
            deathStartMs = Util.getMillis();
            ClientSetting.CompanionCharacter companion = ClientSetting.INSTANCE.companionCharacter.getValue();
            Managers.SOUND.playSound(
                    companion.deathKey(),
                    ClientSetting.INSTANCE.reisaVolume.getValue().floatValue()
            );
        });
    }

    @EventHandler
    private void onRender2D(Render2DEvent.HUD event) {
        if (deathStartMs < 0L) return;

        if (!ClientSetting.INSTANCE.showCompanionOnDeath.getValue()) {
            deathStartMs = -1L;
            return;
        }

        long now     = Util.getMillis();
        long elapsed = now - deathStartMs;

        if (elapsed >= TOTAL_DURATION_MS) {
            deathStartMs = -1L;
            return;
        }

        float alpha = computeAlpha(elapsed);
        if (alpha <= 0.001f) return;

        float slideProgress = elapsed < APPEAR_DURATION_MS
                ? Easing.EASE_OUT_CUBIC.getFunction().apply(
                        Mth.clamp(elapsed / (float) APPEAR_DURATION_MS, 0.0f, 1.0f))
                : 1.0f;

        final var window = mc.getWindow();
        if (renderTarget == null) {
            renderTarget = LuminRenderSystem.LuminRenderTarget.create(
                    "companion-death-hud", window.getWidth(), window.getHeight());
        }
        renderTarget.clear();
        renderTarget.resize(window.getWidth(), window.getHeight());
        LuminRenderSystem.setActiveTarget(renderTarget);

        int width  = LuminRenderSystem.getScaledWidthInt();
        int height = LuminRenderSystem.getScaledHeightInt();

        float imageH = height * 0.55f;
        float imageW = imageH * ASPECT_RATIO;
        float targetX = width  - imageW - 4.0f;
        float startX  = width  + imageW * 0.1f;
        float drawX   = Mth.lerp(slideProgress, startX, targetX);
        float drawY   = height - imageH;

        Identifier texture = resolveTexture();
        Color tint = applyAlpha(Color.WHITE, alpha);

        scene.beginFrame();
        UiTree tree = UiTree.build(scope ->
                scope.layer(-21, layer -> layer.texture(
                        texture,
                        drawX, drawY, imageW, imageH,
                        0.0f, 0.0f, 1.0f, 1.0f,
                        tint, true)));
        scene.submit(UiLayer.CONTENT, tree);
        scene.endFrame();

        LuminRenderSystem.setActiveTarget(null);

        event.getGuiGraphics().blit(
                renderTarget.getIdentifier(),
                0, 0,
                event.getGuiGraphics().guiWidth(),
                event.getGuiGraphics().guiHeight(),
                0, 1, 1, 0);
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        deathStartMs = -1L;
    }

    // ── 内部工具 ─────────────────────────────────────────────────────────────

    private float computeAlpha(long elapsed) {
        if (elapsed < APPEAR_DURATION_MS) {
            return Easing.EASE_OUT_CUBIC.getFunction().apply(
                    Mth.clamp(elapsed / (float) APPEAR_DURATION_MS, 0.0f, 1.0f));
        } else if (elapsed < FADE_START_MS) {
            return 1.0f;
        } else {
            return 1.0f - Easing.EASE_IN_CUBIC.getFunction().apply(
                    Mth.clamp((elapsed - FADE_START_MS) / (float) FADE_DURATION_MS, 0.0f, 1.0f));
        }
    }

    private static Identifier resolveTexture() {
        ClientSetting.CompanionCharacter ch = ClientSetting.INSTANCE.companionCharacter.getValue();
        // Reisa: 16 号（受伤/惊慌）；Hinata: 35 号（悲伤/难过）
        String suffix = ch == ClientSetting.CompanionCharacter.Hinata ? "35" : "16";
        return ResourceLocationUtils.getIdentifier(
                "textures/gui/galgame/" + ch.texturePrefix() + "_" + suffix + ".png");
    }

    private static Color applyAlpha(Color color, float alphaFactor) {
        float factor = Mth.clamp(alphaFactor, 0.0f, 1.0f);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.round(color.getAlpha() * factor));
    }
}

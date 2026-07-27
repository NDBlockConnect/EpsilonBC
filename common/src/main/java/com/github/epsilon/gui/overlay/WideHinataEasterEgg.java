package com.github.epsilon.gui.overlay;

import com.github.epsilon.managers.Managers;
import com.github.epsilon.managers.impl.sound.FadeableSoundInstance;
import com.github.epsilon.managers.impl.sound.SoundKey;
import com.github.epsilon.modules.impl.ClientSetting;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.Random;

/**
 * 宽体 Hinata 彩蛋管理器（单例）。
 * <p>当角色为 Hinata、彩蛋开关开启时，按概率触发「宽体」效果：
 * 在 triggerDuration 秒内将立绘宽高比线性拉至用户设定倍数，
 * 同时以 0.5s 淡入、2s 淡出播放彩蛋音效。
 */
public final class WideHinataEasterEgg {

    public static final WideHinataEasterEgg INSTANCE = new WideHinataEasterEgg();

    /**
     * 彩蛋音效的自然播放时长（秒）。
     * 若实际 OGG 长度不同，调整此常量即可保持 pitch 与时长同步。
     */
    private static final float NATURAL_OGG_DURATION_S = 7.0f;

    private static final long FADE_IN_MS  =   500L;
    private static final long FADE_OUT_MS = 2_000L;

    private final Random random = new Random();

    /** 拉宽动画开始时间戳（毫秒），-1 表示未激活 */
    private long wideStartMs = -1L;
    /** 当前动画时长（毫秒）；每次触发时从设置里读取 */
    private long durationMs  = 3_000L;

    private WideHinataEasterEgg() {}

    // ── 公开接口 ──────────────────────────────────────────────────────────────

    /**
     * 当 Hinata 首次出现时调用。
     * 若角色不是 Hinata、彩蛋未开启、已在播放，或概率未命中，则静默返回。
     */
    public void tryTrigger() {
        // 若上一次动画已自然结束（但 reset() 尚未被调用），允许重新触发
        if (wideStartMs >= 0L) {
            if (Util.getMillis() - wideStartMs < durationMs) return;
            wideStartMs = -1L;
        }
        if (!isHinataSelected()) return;
        ClientSetting cs = ClientSetting.INSTANCE;
        if (!cs.wideHinataEasterEgg.getValue()) return;
        if (random.nextDouble() >= cs.wideHinataProb.getValue()) return;
        doTrigger(cs);
    }

    /**
     * 返回当前帧应使用的宽高比。
     *
     * @param baseRatio 角色的标准宽高比（来自 {@code CompanionCharacter.aspectRatio()}）
     */
    public float getCurrentAspectRatio(float baseRatio) {
        if (wideStartMs < 0L) return baseRatio;
        long elapsed = Util.getMillis() - wideStartMs;
        // 修复：超时后立即返回基准宽高比，避免状态矛盾
        if (elapsed >= durationMs) {
            wideStartMs = -1L;  // 自动重置过期状态
            return baseRatio;
        }
        float t = elapsed / (float) durationMs;  // 已在范围内，不需要 clamp
        float maxMultiplier = ClientSetting.INSTANCE.wideHinataMaxWidth.getValue().floatValue();
        float multiplier = Mth.lerp(t, 1.0f, maxMultiplier);
        return baseRatio * multiplier;
    }

    /** Hinata 消失或场景切换时调用，重置状态。 */
    public void reset() {
        wideStartMs = -1L;
    }

    public boolean isActive() {
        return wideStartMs >= 0L && Util.getMillis() - wideStartMs < durationMs;
    }

    /**
     * 返回本次彩蛋触发的拉宽动画总时长（毫秒）。
     * 供 {@link CompanionDeathOverlay} 计算覆盖层保持时长，
     * 确保用户设定的彩蛋时长能完整播放，而不被覆盖层固定的 5s 窗口截断。
     */
    public long getDurationMs() {
        return durationMs;
    }

    // ── 内部 ──────────────────────────────────────────────────────────────────

    private void doTrigger(ClientSetting cs) {
        durationMs = Math.max(500L, Math.round(cs.wideHinataDuration.getValue() * 1000.0));
        wideStartMs = Util.getMillis();

        float vol = cs.reisaVolume.getValue().floatValue();
        // pitch 固定 1.0f，不做拉伸/慢放；音效以自然时长播放，淡入/淡出纯靠音量控制
        long audioTotalMs = Math.round(NATURAL_OGG_DURATION_S * 1000f);
        mc.getSoundManager().play(
                new FadeableSoundInstance(SoundKey.EASTER_EGG_001, 1.0f, vol,
                        FADE_IN_MS, FADE_OUT_MS, audioTotalMs));
    }

    private static boolean isHinataSelected() {
        return ClientSetting.INSTANCE.companionCharacter.getValue()
                == ClientSetting.CompanionCharacter.Hinata;
    }

    private static final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
}

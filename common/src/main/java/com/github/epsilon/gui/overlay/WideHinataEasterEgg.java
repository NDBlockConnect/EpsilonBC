package com.github.epsilon.gui.overlay;

import com.github.epsilon.managers.Managers;
import com.github.epsilon.managers.impl.sound.SoundKey;
import com.github.epsilon.modules.impl.ClientSetting;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.Random;

/**
 * 宽体 Hinata 彩蛋管理器（单例）。
 *
 * <p>当角色为 Hinata、彩蛋开关开启时，按概率触发"宽体"效果：
 * 在 triggerDuration 秒内将立绘宽高比线性拉至 {@link #MAX_WIDE_MULTIPLIER} 倍，
 * 同时按比例调整音效 pitch 使播放时长与拉宽时长一致。
 *
 * <p>调用方（CompanionDeathOverlay / MainMenuScreen）在以下时机调用：
 * <ul>
 *   <li>{@link #tryTrigger()} — Hinata 首次出现时</li>
 *   <li>{@link #getCurrentAspectRatio(float)} — 每帧渲染时替换原始宽高比</li>
 *   <li>{@link #reset()} — Hinata 消失 / 场景切换时</li>
 * </ul>
 */
public final class WideHinataEasterEgg {

    public static final WideHinataEasterEgg INSTANCE = new WideHinataEasterEgg();

    /** 最大拉宽倍数（相对于角色正常宽高比） */
    private static final float MAX_WIDE_MULTIPLIER = 2.6f;

    /**
     * 彩蛋音效的自然播放时长（秒）。
     * 若实际 OGG 长度不同，调整此常量即可保持 pitch 与时长同步。
     * 当前 OGG：EasterEgg001.ogg ≈ 7MB，预估约 7 秒（stream 模式）。
     * 实测后可微调。
     */
    private static final float NATURAL_OGG_DURATION_S = 7.0f;

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
        if (wideStartMs >= 0L) return;
        if (!isHinataSelected()) return;
        ClientSetting cs = ClientSetting.INSTANCE;
        if (!cs.wideHinataEasterEgg.getValue()) return;
        if (random.nextDouble() >= cs.wideHinataProb.getValue()) return;
        doTrigger(cs);
    }

    /** 立刻触发（ButtonSetting 回调用）。仅当当前角色为 Hinata 时生效。 */
    public void triggerNow() {
        if (!isHinataSelected()) return;
        doTrigger(ClientSetting.INSTANCE);
    }

    /**
     * 返回当前帧应使用的宽高比。
     *
     * @param baseRatio 角色的标准宽高比（来自 {@code CompanionCharacter.aspectRatio()}）
     * @return 可能被拉宽后的宽高比
     */
    public float getCurrentAspectRatio(float baseRatio) {
        if (wideStartMs < 0L) return baseRatio;
        long elapsed = Util.getMillis() - wideStartMs;
        float t = Mth.clamp(elapsed / (float) durationMs, 0.0f, 1.0f);
        float multiplier = Mth.lerp(t, 1.0f, MAX_WIDE_MULTIPLIER);
        return baseRatio * multiplier;
    }

    /** Hinata 消失或场景切换时调用，重置状态。 */
    public void reset() {
        wideStartMs = -1L;
    }

    public boolean isActive() {
        return wideStartMs >= 0L;
    }

    // ── 内部 ──────────────────────────────────────────────────────────────────

    private void doTrigger(ClientSetting cs) {
        durationMs  = Math.max(500L, Math.round(cs.wideHinataDuration.getValue() * 1000.0));
        wideStartMs = Util.getMillis();
        // pitch = 自然时长 / 用户时长，让音效与拉宽动画同步结束
        float pitch = Mth.clamp(NATURAL_OGG_DURATION_S / (durationMs / 1000.0f), 0.5f, 2.0f);
        Managers.SOUND.playSound(SoundKey.EASTER_EGG_001, pitch,
                cs.reisaVolume.getValue().floatValue());
    }

    private static boolean isHinataSelected() {
        return ClientSetting.INSTANCE.companionCharacter.getValue()
                == ClientSetting.CompanionCharacter.Hinata;
    }
}

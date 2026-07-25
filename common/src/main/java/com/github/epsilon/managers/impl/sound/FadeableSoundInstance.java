package com.github.epsilon.managers.impl.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/**
 * 支持淡入/淡出的可 tick 音效实例。
 * 在 {@code totalMs} 毫秒后自动停止；最后 {@code fadeOutMs} 毫秒做线性淡出，
 * 最初 {@code fadeInMs} 毫秒做线性淡入。
 */
public class FadeableSoundInstance extends AbstractTickableSoundInstance {

    private final long startMs;
    private final long fadeInMs;
    private final long fadeOutMs;
    private final long totalMs;
    private final float maxVolume;

    public FadeableSoundInstance(SoundKey key, float pitch, float volume,
                                 long fadeInMs, long fadeOutMs, long totalMs) {
        super(SoundEvent.createVariableRangeEvent(key.id()), SoundSource.UI,
                SoundInstance.createUnseededRandom());
        this.startMs     = Util.getMillis();
        this.fadeInMs    = Math.max(1L, fadeInMs);
        this.fadeOutMs   = Math.max(1L, fadeOutMs);
        this.totalMs     = Math.max(this.fadeInMs + this.fadeOutMs, totalMs);
        this.maxVolume   = Mth.clamp(volume, 0.0f, 1.0f);
        this.pitch       = Mth.clamp(pitch, 0.5f, 2.0f);
        // 初始化为微小正值，防止音效引擎在 tick() 执行前因 volume=0 而拒绝播放
        this.volume      = 0.01f;
        this.looping     = false;
        this.delay       = 0;
        this.attenuation = Attenuation.NONE;
        this.relative    = true;
    }

    @Override
    public void tick() {
        if (isStopped()) return;
        long elapsed = Util.getMillis() - startMs;
        if (elapsed >= totalMs) {
            stop();
            return;
        }
        long fadeOutStart = totalMs - fadeOutMs;
        if (elapsed < fadeInMs) {
            this.volume = maxVolume * (elapsed / (float) fadeInMs);
        } else if (elapsed >= fadeOutStart) {
            float p = (elapsed - fadeOutStart) / (float) fadeOutMs;
            this.volume = maxVolume * Math.max(0.0f, 1.0f - p);
        } else {
            this.volume = maxVolume;
        }
    }
}

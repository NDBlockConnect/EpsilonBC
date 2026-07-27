package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.bus.EventPriority;
import com.github.epsilon.events.impl.MoveEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class Jesus extends Module {

    public static final Jesus INSTANCE = new Jesus();

    private Jesus() {
        super("Jesus", Category.MOVEMENT);
    }

    public enum Mode {
        Water,
        Lava,
        Both
    }

    private final EnumSetting<Mode> mode     = enumSetting("Mode", Mode.Water);
    private final BoolSetting sneakToSink    = boolSetting("Sneak To Sink", true);

    @EventHandler
    private void onMove(MoveEvent event) {
        if (nullCheck()) return;
        if (sneakToSink.getValue() && mc.player.isCrouching()) return;
        if (mc.player.getAbilities().flying || mc.player.isFallFlying()) return;

        boolean inWater = mc.player.isInWater();
        boolean inLava  = mc.player.isInLava();
        boolean active  = switch (mode.getValue()) {
            case Water -> inWater;
            case Lava  -> inLava;
            case Both  -> inWater || inLava;
        };

        if (!active) return;

        // 判断当前接触到哪种流体，取"沉入深度"最大的那一种做决策
        TagKey<Fluid> tag = inLava && (mode.getValue() != Mode.Water) ? FluidTags.LAVA : FluidTags.WATER;
        double fluidDepth = mc.player.getFluidHeight(tag);

        // 眼睛已经淹没：放手让玩家自由潜水/上浮，不再干预
        if (mc.player.isEyeInFluid(FluidTags.WATER) || mc.player.isEyeInFluid(FluidTags.LAVA)) return;
        // 玩家几乎完全没入（脚下没有支撑面，脑袋顶到水面）：也放手，避免"卡在半水中"
        if (fluidDepth >= 0.85) return;

        event.setVertical(0.1, EventPriority.HIGH);
        mc.player.resetFallDistance();
    }
}

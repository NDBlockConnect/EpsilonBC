package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;

public class AutoFish extends Module {

    public static final AutoFish INSTANCE = new AutoFish();

    private AutoFish() {
        super("AutoFish", Category.PLAYER);
    }

    private final DoubleSetting dip   = doubleSetting("Dip Threshold", 0.08, 0.01, 0.3, 0.01);
    private final IntSetting reDelay  = intSetting("Re-throw Delay", 10, 2, 100, 1);

    private enum State { IDLE, WAITING, REELING }
    private State state = State.IDLE;

    private double lastHookY  = 0.0;
    private int    waitTicks  = 0;

    @Override
    protected void onDisable() {
        state     = State.IDLE;
        lastHookY = 0.0;
        waitTicks = 0;
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (!isRodInHand()) return;

        FishingHook hook = findHook();

        switch (state) {
            case IDLE -> {
                if (hook == null) {
                    // No hook in water — cast.
                    use();
                    state = State.WAITING;
                }
            }
            case WAITING -> {
                if (hook == null) {
                    // Hook disappeared (removed before biting) — recast.
                    state = State.IDLE;
                    return;
                }
                double hookY = hook.getY();
                double delta = hookY - lastHookY;
                lastHookY = hookY;
                // A sharp negative dip means a fish bit.
                if (delta < -dip.getValue()) {
                    use();  // reel in
                    waitTicks = 0;
                    state = State.REELING;
                }
            }
            case REELING -> {
                if (hook == null) {
                    // Hook retrieved — wait then recast.
                    if (++waitTicks >= reDelay.getValue()) {
                        use();
                        state = State.WAITING;
                    }
                }
            }
        }
    }

    private boolean isRodInHand() {
        return mc.player.getMainHandItem().getItem() instanceof FishingRodItem
            || mc.player.getOffhandItem().getItem() instanceof FishingRodItem;
    }

    private FishingHook findHook() {
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof FishingHook hook && hook.getPlayerOwner() == mc.player) {
                return hook;
            }
        }
        return null;
    }

    /** Simulate a single right-click with whichever hand holds the rod. */
    private void use() {
        InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof FishingRodItem
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        mc.gameMode.useItem(mc.player, hand);
    }
}

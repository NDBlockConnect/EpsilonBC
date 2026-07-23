package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;

/**
 * Prevents entities from pushing the player by cancelling {@code Entity.push(DDD)}
 * when the entity instance is the local player — see MixinEntity#cancelPush.
 */
public class NoPush extends Module {

    public static final NoPush INSTANCE = new NoPush();

    private NoPush() {
        super("NoPush", Category.MOVEMENT);
    }
}

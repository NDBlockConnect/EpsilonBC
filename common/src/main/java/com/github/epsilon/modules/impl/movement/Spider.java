package com.github.epsilon.modules.impl.movement;

import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;

/**
 * Allows climbing any solid wall by overriding {@code onClimbable()} when the
 * player has a horizontal collision — see MixinLivingEntity#overrideClimbable.
 */
public class Spider extends Module {

    public static final Spider INSTANCE = new Spider();

    private Spider() {
        super("Spider", Category.MOVEMENT);
    }
}

package com.github.epsilon.modules.impl.player;

import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;

/**
 * Removes the right-click placement cooldown by zeroing {@code rightClickDelay}
 * on every client tick — see MixinMinecraft#onPreTick.
 */
public class FastPlace extends Module {

    public static final FastPlace INSTANCE = new FastPlace();

    private FastPlace() {
        super("FastPlace", Category.PLAYER);
    }
}

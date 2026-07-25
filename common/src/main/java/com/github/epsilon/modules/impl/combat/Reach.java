package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;

public class Reach extends Module {

    public static final Reach INSTANCE = new Reach();

    private Reach() {
        super("Reach", Category.COMBAT);
    }

    public final DoubleSetting entityReach = doubleSetting("Entity Reach", 3.0, 3.0, 6.0, 0.1);
    public final BoolSetting overrideBlock = boolSetting("Override Block", false);
    public final DoubleSetting blockReach = doubleSetting("Block Reach", 4.5, 4.5, 6.0, 0.1,
            overrideBlock::getValue);

    @Override
    public String getInfo() {
        return String.format("%.1f", entityReach.getValue());
    }

    public double getEntityReach() {
        return entityReach.getValue();
    }

    public double getBlockReach() {
        return blockReach.getValue();
    }

    public boolean shouldOverrideBlock() {
        return overrideBlock.getValue();
    }

}

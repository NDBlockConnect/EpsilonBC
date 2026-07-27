package com.github.epsilon.modules.impl.render;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.awt.*;

public class Hitboxes extends Module {

    public static final Hitboxes INSTANCE = new Hitboxes();

    private Hitboxes() {
        super("Hitboxes", Category.RENDER);
    }

    public final DoubleSetting expandWidth = doubleSetting("Expand Width", 0.15, 0.0, 0.5, 0.01);
    public final DoubleSetting expandHeight = doubleSetting("Expand Height", 0.1, 0.0, 0.5, 0.01);
    private final BoolSetting render = boolSetting("Render", false);

    public AABB getExpandedAABB(AABB original) {
        double w = expandWidth.getValue();
        double h = expandHeight.getValue();
        return original.inflate(w, h, w);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.getValue() || nullCheck()) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            AABB box = getExpandedAABB(entity.getBoundingBox());
            Managers.GRAPHICS.getRender3DScheduler().addOutlineBox(box, new Color(255, 100, 100, 200));
        }
    }

}

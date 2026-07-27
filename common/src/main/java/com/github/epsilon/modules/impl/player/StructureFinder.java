package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.ColorSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructureFinder extends Module {

    public static final StructureFinder INSTANCE = new StructureFinder();

    private StructureFinder() {
        super("StructureFinder", Category.PLAYER);
    }

    private final IntSetting    radius    = intSetting("Chunk Radius", 8, 1, 32, 1);
    private final BoolSetting   render    = boolSetting("Render", true);
    private final ColorSetting  boxColor  = colorSetting("Color", new Color(255, 215, 0, 160), true, render::getValue);
    private final DoubleSetting thickness = doubleSetting("Thickness", 1.5, 0.5, 4.0, 0.5, render::getValue);
    private final IntSetting    scanEvery = intSetting("Scan Interval", 40, 10, 200, 10);

    private record FoundStructure(String name, AABB box) {}

    private final List<FoundStructure> found = new ArrayList<>();
    private int ticksSinceScan = 0;

    @Override
    protected void onDisable() {
        found.clear();
        ticksSinceScan = 0;
    }

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.valueOf(found.size());
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (nullCheck()) return;
        if (++ticksSinceScan < scanEvery.getValue()) return;
        ticksSinceScan = 0;
        scan();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.getValue() || found.isEmpty()) return;
        Color boxColorValue = boxColor.getValue();
        float thick = thickness.getValue().floatValue();
        for (FoundStructure s : found) {
            Managers.GRAPHICS.getRender3DScheduler().addOutlineBox(s.box(), boxColorValue, thick);
        }
    }

    private void scan() {
        found.clear();
        int playerCX = mc.player.chunkPosition().x();
        int playerCZ = mc.player.chunkPosition().z();
        int r = radius.getValue();

        for (int cx = playerCX - r; cx <= playerCX + r; cx++) {
            for (int cz = playerCZ - r; cz <= playerCZ + r; cz++) {
                if (!mc.level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = mc.level.getChunk(cx, cz);
                Map<Structure, StructureStart> starts = chunk.getAllStarts();
                if (starts.isEmpty()) continue;

                for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
                    StructureStart start = entry.getValue();
                    if (!start.isValid()) continue;

                    String name = structureName(entry.getKey());
                    var bb = start.getBoundingBox();
                    AABB box = new AABB(bb.minX(), bb.minY(), bb.minZ(),
                                       bb.maxX() + 1, bb.maxY() + 1, bb.maxZ() + 1);
                    found.add(new FoundStructure(name, box));
                }
            }
        }
    }

    private String structureName(Structure structure) {
        try {
            var registry = mc.level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var key = registry.getKey(structure);
            return key != null ? key.getPath() : structure.getClass().getSimpleName();
        } catch (Exception e) {
            return structure.getClass().getSimpleName();
        }
    }
}

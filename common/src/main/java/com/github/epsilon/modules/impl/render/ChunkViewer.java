package com.github.epsilon.modules.impl.render;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PacketEvent;
import com.github.epsilon.events.impl.Render3DEvent;
import com.github.epsilon.graphics.schedulers.render3d.Render3DScheduler;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.ColorSetting;
import com.github.epsilon.settings.impl.DoubleSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkViewer extends Module {

    public static final ChunkViewer INSTANCE = new ChunkViewer();

    private ChunkViewer() {
        super("ChunkViewer", Category.RENDER);
    }

    public enum Mode {
        // Outline every chunk currently loaded by the server.
        Loaded,
        // Only highlight chunks as they arrive, fading out over time.
        New
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.New);
    private final BoolSetting fullHeight = boolSetting("Full Height", false);
    private final DoubleSetting height = doubleSetting("Height Offset", 0.0, -64.0, 64.0, 1.0, () -> !fullHeight.getValue());
    private final DoubleSetting thickness = doubleSetting("Thickness", 1.5, 0.5, 5.0, 0.1);
    private final IntSetting fadeTime = intSetting("Fade Time", 8000, 500, 60000, 500, () -> mode.is(Mode.New));
    private final ColorSetting color = colorSetting("Color", new Color(0, 255, 120, 200), true);

    // Packed chunk pos -> load timestamp (ms). Packet handlers run off the render thread,
    // so this must stay concurrent-safe while Render3D iterates it.
    private final Map<Long, Long> chunks = new ConcurrentHashMap<>();

    @Override
    protected void onDisable() {
        chunks.clear();
    }

    @Override
    public String getInfo() {
        if (nullCheck()) return null;
        return String.valueOf(chunks.size());
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundLevelChunkWithLightPacket packet) {
            chunks.put(ChunkPos.pack(packet.getX(), packet.getZ()), System.currentTimeMillis());
        } else if (event.getPacket() instanceof ClientboundForgetLevelChunkPacket packet) {
            chunks.remove(packet.pos().pack());
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (nullCheck() || chunks.isEmpty()) return;

        long now = System.currentTimeMillis();
        boolean newMode = mode.is(Mode.New);
        int fade = fadeTime.getValue();
        Color base = color.getValue();
        float thick = thickness.getValue().floatValue();

        double y0;
        double y1;
        if (fullHeight.getValue()) {
            y0 = mc.level.getMinY();
            y1 = mc.level.getMaxY();
        } else {
            y0 = mc.player.getY() + height.getValue();
            y1 = y0;
        }

        for (Map.Entry<Long, Long> entry : chunks.entrySet()) {
            long key = entry.getKey();
            long age = now - entry.getValue();

            int alpha = base.getAlpha();
            if (newMode) {
                if (age > fade) continue;
                // Linear fade from full opacity down to zero across the fade window.
                alpha = (int) (base.getAlpha() * (1.0 - (double) age / fade));
                if (alpha <= 0) continue;
            }

            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            double x0 = cx << 4;
            double z0 = cz << 4;

            AABB box = new AABB(x0, y0, z0, x0 + 16.0, y1, z0 + 16.0);
            int rgb = new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha).getRGB();
            Render3DScheduler.INSTANCE.addOutlineBox(box, rgb, thick);
        }
    }

}

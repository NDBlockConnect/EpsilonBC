package com.github.epsilon.platform;

import net.minecraft.client.Minecraft;

/**
 * Minecraft 访问提供者
 * 由 common 层在启动时注入具体实现
 */
public class MinecraftProvider {
    private static IMinecraftAccess instance;

    public static void setMinecraftAccess(IMinecraftAccess access) {
        instance = access;
    }

    public static IMinecraftAccess get() {
        if (instance == null) {
            // 降级到直接使用 Minecraft.getInstance()
            Minecraft mc = Minecraft.getInstance();
            return new IMinecraftAccess() {
                @Override
                public net.minecraft.client.player.LocalPlayer getPlayer() {
                    return mc.player;
                }

                @Override
                public net.minecraft.client.multiplayer.ClientLevel getLevel() {
                    return mc.level;
                }

                @Override
                public boolean isPlayerNull() {
                    return mc.player == null;
                }

                @Override
                public boolean isLevelNull() {
                    return mc.level == null;
                }
            };
        }
        return instance;
    }
}

package com.github.epsilon.platform;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

/**
 * Minecraft 客户端访问接口
 * 由 common 层在启动时注入具体实现
 */
public interface IMinecraftAccess {
    LocalPlayer getPlayer();

    ClientLevel getLevel();

    boolean isPlayerNull();

    boolean isLevelNull();
}

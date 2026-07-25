package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.ClientTickEvent;
import com.github.epsilon.events.impl.GameJoinedEvent;
import com.github.epsilon.events.impl.OpenScreenEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class AutoReconnect extends Module {

    public static final AutoReconnect INSTANCE = new AutoReconnect();

    private AutoReconnect() {
        super("AutoReconnect", Category.PLAYER);
    }

    private final IntSetting delay = intSetting("Delay", 5, 1, 60, 1);

    // Vanilla clears the current-server reference on disconnect, so we cache it
    // the moment we successfully join a world and reuse it to reconnect.
    private ServerData lastServer;
    // -1 = idle; otherwise the number of client ticks remaining before we retry.
    private int reconnectTicks = -1;

    @Override
    protected void onDisable() {
        reconnectTicks = -1;
    }

    @Override
    public String getInfo() {
        if (reconnectTicks < 0) return null;
        return (reconnectTicks / 20 + 1) + "s";
    }

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        ServerData current = mc.getCurrentServer();
        if (current != null) {
            lastServer = current;
        }
        // A fresh join means any pending retry is done.
        reconnectTicks = -1;
    }

    @EventHandler
    private void onScreen(OpenScreenEvent event) {
        if (!(event.getScreen() instanceof DisconnectedScreen)) return;
        if (lastServer == null) return;
        reconnectTicks = delay.getValue() * 20;
    }

    @EventHandler
    private void onTick(ClientTickEvent.Pre event) {
        if (reconnectTicks < 0) return;
        // Bail out the instant the user navigates away from the disconnect screen.
        if (!(mc.gui.screen() instanceof DisconnectedScreen)) {
            reconnectTicks = -1;
            return;
        }
        if (reconnectTicks == 0) {
            reconnectTicks = -1;
            reconnect();
            return;
        }
        reconnectTicks--;
    }

    private void reconnect() {
        if (lastServer == null) return;
        try {
            ServerAddress address = ServerAddress.parseString(lastServer.ip);
            ConnectScreen.startConnecting(new TitleScreen(), mc, address, lastServer, false, null);
        } catch (Exception ignored) {
        }
    }
}

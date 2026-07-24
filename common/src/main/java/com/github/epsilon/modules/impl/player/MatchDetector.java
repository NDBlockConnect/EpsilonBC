package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.GameLeftEvent;
import com.github.epsilon.events.impl.PacketEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.StringListSetting;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.util.List;
import java.util.Locale;

/**
 * Detects the start and end of an HvH match by sniffing server-sent title/subtitle/
 * action-bar/system-chat text against configurable keyword lists, and resets match-scoped
 * ally marks accordingly. Keywords are lowercased substring matches (the server's exact
 * wording is server-specific, so this is data-driven rather than hardcoded).
 *
 * PacketEvent.Receive fires on the netty thread, so the actual mark reset (which touches
 * FriendManager) is deferred to the main thread via mc.execute.
 */
public class MatchDetector extends Module {

    public static final MatchDetector INSTANCE = new MatchDetector();

    private MatchDetector() {
        super("Match Detector", Category.PLAYER);
    }

    private final StringListSetting startKeywords = stringListSetting("Start Keywords",
            List.of("match starting", "对局开始", "game start"));
    private final StringListSetting endKeywords = stringListSetting("End Keywords",
            List.of("match over", "round over", "对局结束", "you won", "you lost", "victory", "defeat"));

    private final BoolSetting scanTitle = boolSetting("Scan Title", true);
    private final BoolSetting scanSubtitle = boolSetting("Scan Subtitle", true);
    private final BoolSetting scanActionBar = boolSetting("Scan Action Bar", false);
    private final BoolSetting scanChat = boolSetting("Scan Chat", true);
    private final BoolSetting resetOnStart = boolSetting("Reset On Start", true);
    private final BoolSetting resetOnEnd = boolSetting("Reset On End", true);
    private final BoolSetting resetOnLeave = boolSetting("Reset On Leave", true);
    private final BoolSetting notify = boolSetting("Notify", true);

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        String text = switch (event.getPacket()) {
            case ClientboundSetTitleTextPacket p when scanTitle.getValue() -> p.text().getString();
            case ClientboundSetSubtitleTextPacket p when scanSubtitle.getValue() -> p.text().getString();
            case ClientboundSetActionBarTextPacket p when scanActionBar.getValue() -> p.text().getString();
            case ClientboundSystemChatPacket p when scanChat.getValue() -> p.content().getString();
            default -> null;
        };
        if (text == null || text.isBlank()) return;

        String lower = text.toLowerCase(Locale.ROOT);
        boolean started = resetOnStart.getValue() && matchesAny(lower, startKeywords.getValue());
        boolean ended = resetOnEnd.getValue() && matchesAny(lower, endKeywords.getValue());
        if (!started && !ended) return;

        String phase = started ? "start" : "end";
        // Defer to the main thread: FriendManager mutation must not run on netty.
        mc.execute(() -> {
            Managers.ALLY.clearMatchScoped();
            if (notify.getValue()) log("Match " + phase + " detected — cleared match-scoped ally marks");
        });
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!resetOnLeave.getValue()) return;
        Managers.ALLY.clearMatchScoped();
    }

    private boolean matchesAny(String haystackLower, List<String> keywords) {
        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) continue;
            if (haystackLower.contains(kw.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private void log(String message) {
        Managers.NOTIFICATION.info("Match Detector", message);
    }
}

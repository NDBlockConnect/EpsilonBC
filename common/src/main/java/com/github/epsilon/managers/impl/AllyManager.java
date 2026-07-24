package com.github.epsilon.managers.impl;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.PlayerTickEvent;
import com.github.epsilon.managers.Managers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player/entity allies marked at runtime (e.g. via middle-click). This is a
 * superset of {@link FriendManager}:
 * <ul>
 *   <li>FriendManager keys on player <b>name</b> only, has no lifetime and is what the
 *       combat side (TargetManager / AimBot) already checks. Marking a <b>player</b> ally
 *       therefore also writes into FriendManager so attacks skip them for free.</li>
 *   <li>AllyManager keys on <b>UUID</b> and carries an expiry + scope, so it can also mark
 *       non-player entities (mobs have no name) and expire marks over time. The visual side
 *       (EnemyView etc.) queries {@link #isAlly(Entity)} to exempt allies from ESP/chams.</li>
 * </ul>
 * Subscribed to the event bus independently of any module so marks (and their combat
 * exemption) stay valid even when the marking module is toggled off.
 */
public class AllyManager {

    /** How long a mark lives. */
    public enum Scope {
        /** Never expires automatically; only manual unmark or a full clear removes it. */
        PERMANENT,
        /** Expires after a fixed duration. */
        TIMED,
        /** Lives until the current match ends (cleared by MatchDetector). */
        MATCH
    }

    private record Mark(long expiry, String playerName, Scope scope) {
        boolean expired(long now) {
            return now >= expiry;
        }
    }

    // Written from the main thread (middle-click, tick) and potentially cleared from the
    // netty thread (packet-driven match detection defers to main, but be safe anyway).
    private final Map<UUID, Mark> allies = new ConcurrentHashMap<>();

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        long now = System.currentTimeMillis();
        allies.entrySet().removeIf(e -> {
            if (e.getValue().expired(now)) {
                releaseFriend(e.getValue());
                return true;
            }
            return false;
        });
    }

    /**
     * Mark an entity as an ally.
     *
     * @param durationMs only used when {@code scope == TIMED}; ignored otherwise.
     */
    public void mark(Entity entity, Scope scope, long durationMs) {
        if (entity == null) return;
        long expiry = scope == Scope.TIMED ? System.currentTimeMillis() + Math.max(0L, durationMs) : Long.MAX_VALUE;
        String name = entity instanceof Player player ? player.getGameProfile().name() : null;
        allies.put(entity.getUUID(), new Mark(expiry, name, scope));
        if (name != null) Managers.FRIEND.addFriend(name);
    }

    /** Remove an existing mark. Returns true if the entity was marked. */
    public boolean unmark(Entity entity) {
        if (entity == null) return false;
        Mark removed = allies.remove(entity.getUUID());
        if (removed == null) return false;
        releaseFriend(removed);
        return true;
    }

    public boolean isAlly(Entity entity) {
        if (entity == null) return false;
        Mark mark = allies.get(entity.getUUID());
        return mark != null && !mark.expired(System.currentTimeMillis());
    }

    public boolean isMarked(UUID id) {
        Mark mark = allies.get(id);
        return mark != null && !mark.expired(System.currentTimeMillis());
    }

    /**
     * Clear marks scoped to a single match (TIMED + MATCH). PERMANENT marks — the ones the
     * player wants to keep long-term — are preserved. Called by MatchDetector on round
     * start/end. Must be invoked on the main thread since it touches FriendManager.
     */
    public void clearMatchScoped() {
        allies.entrySet().removeIf(e -> {
            if (e.getValue().scope() != Scope.PERMANENT) {
                releaseFriend(e.getValue());
                return true;
            }
            return false;
        });
    }

    /** Clear every mark regardless of scope. Must run on the main thread. */
    public void clearAll() {
        for (Mark mark : allies.values()) releaseFriend(mark);
        allies.clear();
    }

    public int size() {
        return allies.size();
    }

    public List<UUID> getAllies() {
        return new ArrayList<>(allies.keySet());
    }

    private void releaseFriend(Mark mark) {
        if (mark.playerName() != null) Managers.FRIEND.removeFriend(mark.playerName());
    }
}

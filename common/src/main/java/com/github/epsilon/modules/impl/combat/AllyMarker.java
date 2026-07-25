package com.github.epsilon.modules.impl.combat;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.MousePressEvent;
import com.github.epsilon.managers.Managers;
import com.github.epsilon.managers.impl.AllyManager;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.IntSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Middle-click the entity under the crosshair to toggle it as an ally. Allies are exempt
 * from both combat (TargetManager/AimBot already skip FriendManager entries, which a marked
 * player is written into) and the EnemyView visuals (which query {@link AllyManager#isAlly}).
 * Marks carry a scope so they can auto-expire after a duration or at match end.
 */
public class AllyMarker extends Module {

    public static final AllyMarker INSTANCE = new AllyMarker();

    private AllyMarker() {
        super("Ally Marker", Category.COMBAT);
    }

    private final EnumSetting<AllyManager.Scope> scope =
            enumSetting("Scope", AllyManager.Scope.MATCH);
    private final IntSetting timedSeconds =
            intSetting("Timed Seconds", 30, 1, 600, 1, () -> scope.is(AllyManager.Scope.TIMED));
    private final BoolSetting markEntities =
            boolSetting("Mark Entities", true);
    private final BoolSetting requireLiving =
            boolSetting("Only Living", true);
    private final BoolSetting notify =
            boolSetting("Notify", true);

    @EventHandler
    private void onMouse(MousePressEvent event) {
        if (nullCheck()) return;
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != GLFW.GLFW_PRESS) return;
        if (mc.screen != null) return;

        if (!(mc.hitResult instanceof EntityHitResult ehr)) return;
        Entity target = ehr.getEntity();
        if (target == null || target == mc.player) return;

        boolean isPlayer = target instanceof Player;
        if (!isPlayer && !markEntities.getValue()) return;
        if (requireLiving.getValue() && !(target instanceof LivingEntity)) return;

        // We handled the crosshair entity — swallow the click so vanilla pick-block does
        // not also fire.
        event.cancel();

        if (Managers.ALLY.unmark(target)) {
            if (notify.getValue()) log("Unmarked " + describe(target));
            return;
        }

        long durationMs = timedSeconds.getValue() * 1000L;
        Managers.ALLY.mark(target, scope.getValue(), durationMs);
        if (notify.getValue()) log("Marked " + describe(target) + " as ally (" + scope.getValue() + ")");
    }

    private String describe(Entity entity) {
        if (entity instanceof Player player) return player.getGameProfile().name();
        return entity.getType().getDescription().getString();
    }

    private void log(String message) {
        Managers.NOTIFICATION.info("Ally Marker", message);
    }
}

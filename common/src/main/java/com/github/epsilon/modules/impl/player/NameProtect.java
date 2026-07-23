package com.github.epsilon.modules.impl.player;

import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.GameJoinedEvent;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.BoolSetting;
import com.github.epsilon.settings.impl.StringListSetting;
import com.github.epsilon.settings.impl.StringSetting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces the local player's own name (and any extra listed names) in rendered
 * text with a placeholder, for privacy while streaming or sharing screenshots.
 *
 * <p>The heavy lifting lives in {@code MixinFont}, which calls {@link #process(String)}
 * on the {@code String} and {@link Component} {@code drawInBatch} overloads. Purely
 * client-side: nothing is sent to the server.</p>
 */
public class NameProtect extends Module {

    public static final NameProtect INSTANCE = new NameProtect();

    private NameProtect() {
        super("NameProtect", Category.PLAYER);
    }

    private final StringSetting replacement = stringSetting("Replacement", "You");
    private final BoolSetting protectOwnName = boolSetting("Protect Own Name", true);
    private final StringListSetting extraNames = stringListSetting("Extra Names", List.of());

    // Cached each join so we do not hit the profile API every render call.
    private volatile String ownName = "";

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        cacheOwnName();
    }

    private void cacheOwnName() {
        try {
            if (mc.getUser() != null) {
                ownName = mc.getUser().getName();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Called from MixinFont on every text draw. Returns the input untouched when
     * the module is disabled so the hot render path stays cheap.
     */
    public String process(String text) {
        if (!isEnabled() || text == null || text.isEmpty()) return text;

        String result = text;
        String rep = replacement.getValue();

        if (protectOwnName.getValue()) {
            if (ownName.isEmpty()) cacheOwnName();
            if (!ownName.isEmpty() && result.contains(ownName)) {
                result = result.replace(ownName, rep);
            }
        }

        List<String> extras = extraNames.getValue();
        if (!extras.isEmpty()) {
            for (String name : extras) {
                if (name != null && !name.isEmpty() && result.contains(name)) {
                    result = result.replace(name, rep);
                }
            }
        }
        return result;
    }

    /** True when NameProtect should rewrite text right now. */
    public boolean shouldProcess() {
        if (!isEnabled()) return false;
        if (protectOwnName.getValue()) return true;
        return !extraNames.getValue().isEmpty();
    }

    /** Convenience for the Component draw path. Rebuilds a literal (styling is dropped). */
    public Component process(Component component) {
        if (component == null) return component;
        String original = component.getString();
        String processed = process(original);
        if (processed.equals(original)) return component;
        return Component.literal(processed);
    }

    // Kept to avoid an unused-import style warning path if extras logic changes.
    @SuppressWarnings("unused")
    private List<String> snapshotExtras() {
        return new ArrayList<>(extraNames.getValue());
    }
}

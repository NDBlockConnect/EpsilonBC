package com.github.epsilon.modules.impl.player;

import com.github.epsilon.managers.impl.account.AccountManager;
import com.github.epsilon.modules.Category;
import com.github.epsilon.modules.Module;
import com.github.epsilon.settings.impl.ButtonSetting;
import com.github.epsilon.settings.impl.EnumSetting;
import com.github.epsilon.settings.impl.StringSetting;
import com.github.epsilon.utils.player.ChatUtils;

/**
 * In-game account switcher. Exposes the two feasible login paths through the
 * standard settings UI (no bespoke screen needed):
 *
 * <ul>
 *   <li><b>Offline</b> — type a username and click <i>Login</i>; the client
 *       rebuilds the session with the vanilla offline UUID. Works on
 *       offline-mode servers.</li>
 *   <li><b>Token</b> — paste a real access token plus the matching name (UUID
 *       optional, resolved from the name when blank) and click <i>Login</i> for
 *       a premium session.</li>
 * </ul>
 *
 * <p>The heavy lifting lives in {@link AccountManager}. Switching is refused
 * while connected to a world. Enabling the module has no side effect; the
 * buttons do the work.</p>
 */
public class AltManager extends Module {

    public static final AltManager INSTANCE = new AltManager();

    public enum Mode {
        Offline,
        Token
    }

    private AltManager() {
        super("AltManager", Category.PLAYER);
    }

    private final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Offline);

    private final StringSetting username = stringSetting("Username", "");

    private final StringSetting uuid = stringSetting("UUID", "", () -> mode.is(Mode.Token));

    private final StringSetting token = stringSetting("Access Token", "", () -> mode.is(Mode.Token));

    @SuppressWarnings("unused")
    private final ButtonSetting login = buttonSetting("Login", this::doLogin);

    @SuppressWarnings("unused")
    private final ButtonSetting resolveName =
            buttonSetting("Resolve Name From UUID", this::doResolveName, () -> mode.is(Mode.Token));

    @SuppressWarnings("unused")
    private final ButtonSetting restore = buttonSetting("Restore Original", this::doRestore);

    private void doLogin() {
        AccountManager.Result result = switch (mode.getValue()) {
            case Offline -> AccountManager.INSTANCE.loginOffline(username.getValue());
            case Token -> AccountManager.INSTANCE.loginToken(
                    username.getValue(), uuid.getValue(), token.getValue());
        };
        ChatUtils.addChatMessage(result.message());
    }

    private void doResolveName() {
        String resolved = AccountManager.INSTANCE.resolveNameFromUuid(uuid.getValue());
        if (resolved != null && !resolved.isBlank()) {
            username.setValue(resolved);
            ChatUtils.addChatMessage("Resolved name: " + resolved);
        } else {
            ChatUtils.addChatMessage("Could not resolve a name for that UUID.");
        }
    }

    private void doRestore() {
        ChatUtils.addChatMessage(AccountManager.INSTANCE.restoreOriginal().message());
    }
}

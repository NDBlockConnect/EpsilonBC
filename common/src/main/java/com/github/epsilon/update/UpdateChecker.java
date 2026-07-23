package com.github.epsilon.update;

import com.github.epsilon.Constants;
import com.github.epsilon.events.bus.EventBus;
import com.github.epsilon.events.bus.EventHandler;
import com.github.epsilon.events.impl.GameJoinedEvent;
import com.github.epsilon.modules.impl.ClientSetting;
import com.github.epsilon.utils.player.ChatUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Asynchronously queries the GitHub releases API on startup and, if a newer
 * release than the running build is published, notifies the player in chat the
 * first time they join a world.
 *
 * <p>Purely a convenience check: no telemetry is sent, only a single GET to the
 * public releases endpoint. Fails silently when offline or rate-limited.</p>
 */
public class UpdateChecker {

    public static final UpdateChecker INSTANCE = new UpdateChecker();

    private static final String RELEASES_API =
            "https://api.github.com/repos/NDBlockConnect/EpsilonBC/releases/latest";

    // Populated by the async check; read on the client tick / join thread.
    private volatile boolean updateAvailable = false;
    private volatile String latestVersion = "";
    private volatile String releaseUrl = "";
    private volatile boolean notified = false;

    private UpdateChecker() {
        EventBus.INSTANCE.subscribe(this);
    }

    /** Kicks off the async check. Safe to call once during client init. */
    public void init() {
        if (!ClientSetting.INSTANCE.checkForUpdates.getValue()) return;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RELEASES_API))
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", Constants.NAME + "-UpdateChecker")
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(this::handleResponse)
                .exceptionally(t -> {
                    Constants.LOGGER.debug("Update check failed: {}", t.toString());
                    return null;
                });
    }

    private void handleResponse(HttpResponse<String> response) {
        try {
            if (response.statusCode() != 200) return;
            JsonElement root = JsonParser.parseString(response.body());
            if (!root.isJsonObject()) return;
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("draft") && obj.get("draft").getAsBoolean()) return;
            if (obj.has("prerelease") && obj.get("prerelease").getAsBoolean()) return;
            if (!obj.has("tag_name")) return;

            String tag = obj.get("tag_name").getAsString();
            String remote = normalize(tag);
            String local = normalize(Constants.VERSION);

            if (compareVersions(remote, local) > 0) {
                latestVersion = tag;
                releaseUrl = obj.has("html_url") ? obj.get("html_url").getAsString() : "";
                updateAvailable = true;
                Constants.LOGGER.info("A newer {} release is available: {}", Constants.NAME, tag);
            } else {
                Constants.LOGGER.info("{} is up to date ({}).", Constants.NAME, Constants.VERSION);
            }
        } catch (Exception e) {
            Constants.LOGGER.debug("Update response parse failed: {}", e.toString());
        }
    }

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        if (!updateAvailable || notified) return;
        if (!ClientSetting.INSTANCE.checkForUpdates.getValue()) return;
        notified = true;

        Component message = Component.literal("New version ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(latestVersion).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" is available (you have " + Constants.DISPLAY_VERSION + ").")
                        .withStyle(ChatFormatting.GREEN));

        if (!releaseUrl.isEmpty()) {
            message = message.copy().append(Component.literal(" [Download]")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create(releaseUrl)))));
        }

        ChatUtils.addChatMessage(message);
    }

    /** Strips a leading 'v' and any build metadata, keeping the numeric+prerelease core. */
    private static String normalize(String version) {
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        int plus = v.indexOf('+');
        if (plus >= 0) v = v.substring(0, plus);
        return v;
    }

    /**
     * Compares two normalized SemVer-ish strings. Returns >0 if a is newer than b.
     * Compares the numeric release triple first; a release with no pre-release tag
     * outranks the same release with one (1.0.0 > 1.0.0-alpha).
     */
    static int compareVersions(String a, String b) {
        String[] aParts = a.split("-", 2);
        String[] bParts = b.split("-", 2);

        int releaseCmp = compareRelease(aParts[0], bParts[0]);
        if (releaseCmp != 0) return releaseCmp;

        boolean aPre = aParts.length > 1 && !aParts[1].isEmpty();
        boolean bPre = bParts.length > 1 && !bParts[1].isEmpty();
        if (aPre && !bPre) return -1;
        if (!aPre && bPre) return 1;
        if (aPre) return aParts[1].compareTo(bParts[1]);
        return 0;
    }

    private static int compareRelease(String a, String b) {
        String[] as = a.split("\\.");
        String[] bs = b.split("\\.");
        int len = Math.max(as.length, bs.length);
        for (int i = 0; i < len; i++) {
            int av = i < as.length ? parseIntSafe(as[i]) : 0;
            int bv = i < bs.length ? parseIntSafe(bs[i]) : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("\\D", "").isEmpty() ? "0" : s.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

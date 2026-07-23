package com.github.epsilon.managers.impl.account;

import com.github.epsilon.Constants;
import com.github.epsilon.mixins.MixinMinecraftAccessor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Swaps the active {@link User} on the running {@link Minecraft} instance at
 * runtime. Two account kinds are supported:
 *
 * <ul>
 *   <li><b>Offline</b> — username only; the UUID is derived the same way the
 *       vanilla offline path does ({@code OfflinePlayer:&lt;name&gt;}). Works on
 *       offline-mode / cracked servers.</li>
 *   <li><b>Session token</b> — a real Microsoft/Mojang access token plus the
 *       matching name and UUID. This is the only way to authenticate as a
 *       premium account: a valid token cannot be derived from a UUID alone, it
 *       has to be issued by Microsoft's auth flow and pasted in.</li>
 * </ul>
 *
 * <p>Switching mutates both {@code user} and {@code profileFuture} via
 * {@link MixinMinecraftAccessor}. {@code profileFuture} is reset to a completed
 * {@code null} so {@code Minecraft#getGameProfile()} rebuilds the profile from
 * the new user instead of serving the previous account's cached result.</p>
 */
public final class AccountManager {

    public static final AccountManager INSTANCE = new AccountManager();

    private static final String NAME_TO_UUID =
            "https://api.mojang.com/users/profiles/minecraft/";
    private static final String UUID_TO_PROFILE =
            "https://sessionserver.mojang.com/session/minecraft/profile/";

    private final Minecraft mc = Minecraft.getInstance();

    // Captured lazily the first time we switch, so "restore" can put it back.
    private User originalUser;
    private boolean originalCaptured;

    private AccountManager() {
    }

    /** Result of a switch attempt, carrying a human-readable message for chat. */
    public record Result(boolean success, String message) {
    }

    /**
     * Logs in as an offline account. The switch is refused while connected to a
     * world to avoid a half-swapped session.
     */
    public Result loginOffline(String name) {
        if (name == null || name.isBlank()) {
            return new Result(false, "Username is empty.");
        }
        if (mc.level != null) {
            return new Result(false, "Disconnect before switching accounts.");
        }
        String trimmed = name.trim();
        UUID uuid = offlineUuid(trimmed);
        return applyUser(new User(trimmed, uuid, "", Optional.empty(), Optional.empty()),
                "Switched to offline account " + trimmed + ".");
    }

    /**
     * Logs in with a real session (access) token. {@code name} and {@code uuid}
     * must match the token's account. When {@code uuid} is blank it is resolved
     * from the name via Mojang's public API.
     */
    public Result loginToken(String name, String uuidString, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return new Result(false, "Access token is empty.");
        }
        if (name == null || name.isBlank()) {
            return new Result(false, "Username is empty.");
        }
        if (mc.level != null) {
            return new Result(false, "Disconnect before switching accounts.");
        }

        String trimmedName = name.trim();
        UUID uuid;
        try {
            uuid = (uuidString == null || uuidString.isBlank())
                    ? resolveUuidFromName(trimmedName)
                    : parseUuid(uuidString.trim());
        } catch (Exception e) {
            return new Result(false, "Could not resolve UUID: " + e.getMessage());
        }
        if (uuid == null) {
            return new Result(false, "Could not resolve a UUID for " + trimmedName + ".");
        }

        return applyUser(new User(trimmedName, uuid, accessToken.trim(), Optional.empty(), Optional.empty()),
                "Switched to premium account " + trimmedName + ".");
    }

    /** Restores the account the client started with, if it was ever replaced. */
    public Result restoreOriginal() {
        if (!originalCaptured || originalUser == null) {
            return new Result(false, "No original account to restore.");
        }
        if (mc.level != null) {
            return new Result(false, "Disconnect before switching accounts.");
        }
        return applyUser(originalUser, "Restored original account " + originalUser.getName() + ".");
    }

    /** Current in-use account name, for display. */
    public String currentName() {
        User user = mc.getUser();
        return user != null ? user.getName() : "unknown";
    }

    private Result applyUser(User user, String successMessage) {
        try {
            if (!originalCaptured) {
                originalUser = mc.getUser();
                originalCaptured = true;
            }
            MixinMinecraftAccessor accessor = (MixinMinecraftAccessor) (Object) mc;
            accessor.epsilon$setUser(user);
            accessor.epsilon$setProfileFuture(CompletableFuture.completedFuture(null));
            Constants.LOGGER.info("Account switched to {} ({}).", user.getName(), user.getProfileId());
            return new Result(true, successMessage);
        } catch (Exception e) {
            Constants.LOGGER.error("Account switch failed", e);
            return new Result(false, "Switch failed: " + e);
        }
    }

    /** Mirrors the vanilla offline UUID derivation. */
    private static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    /** Accepts both dashed and undashed 32-hex UUID forms. */
    private static UUID parseUuid(String raw) {
        if (raw.contains("-")) {
            return UUID.fromString(raw);
        }
        if (raw.length() != 32) {
            throw new IllegalArgumentException("expected 32 hex chars");
        }
        String dashed = raw.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(dashed);
    }

    /** Blocking lookup of a UUID from a username via Mojang's public API. */
    private static UUID resolveUuidFromName(String name) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NAME_TO_UUID + name))
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!obj.has("id")) {
            return null;
        }
        return parseUuid(obj.get("id").getAsString());
    }

    /**
     * Blocking lookup of the current name for a UUID via Mojang's session
     * server. Useful when the player only knows the account UUID.
     */
    public String resolveNameFromUuid(String uuidString) {
        try {
            UUID uuid = parseUuid(uuidString.trim());
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UUID_TO_PROFILE + uuid.toString().replace("-", "")))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }
            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
            return obj.has("name") ? obj.get("name").getAsString() : null;
        } catch (Exception e) {
            Constants.LOGGER.debug("Name lookup failed: {}", e.toString());
            return null;
        }
    }
}

package com.github.epsilon.mixins;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

/**
 * Exposes setters for {@link Minecraft}'s otherwise-final {@code user} and
 * {@code profileFuture} fields so the client can swap the active account at
 * runtime (see {@code AccountManager}).
 *
 * <p>{@code profileFuture} is reset alongside {@code user} because
 * {@code Minecraft#getGameProfile()} reads it first; leaving a stale future
 * would keep reporting the previous account's profile.</p>
 */
@Mixin(Minecraft.class)
public interface MixinMinecraftAccessor {

    @Mutable
    @Accessor("user")
    void epsilon$setUser(User user);

    @Mutable
    @Accessor("profileFuture")
    void epsilon$setProfileFuture(CompletableFuture<ProfileResult> profileFuture);
}

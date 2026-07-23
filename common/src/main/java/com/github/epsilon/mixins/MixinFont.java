package com.github.epsilon.mixins;

import com.github.epsilon.graphics.text.minecraft.EpsilonFontGlyph;
import com.github.epsilon.graphics.text.minecraft.EpsilonFontMetrics;
import com.github.epsilon.modules.impl.ClientSetting;
import com.github.epsilon.modules.impl.player.NameProtect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class MixinFont {

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
    private void onGetGlyph(int codepoint, Style style, CallbackInfoReturnable<BakedGlyph> cir) {
        if (ClientSetting.INSTANCE.replaceMinecraftFont.getValue()) {
            EpsilonFontGlyph glyph = EpsilonFontGlyph.create(codepoint);
            if (glyph != null) {
                cir.setReturnValue(glyph);
            }
        }
    }

    @Inject(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void onWidthString(String text, CallbackInfoReturnable<Integer> cir) {
        if (ClientSetting.INSTANCE.replaceMinecraftFont.getValue()) {
            Float width = EpsilonFontMetrics.width(text);
            if (width != null) {
                cir.setReturnValue(Mth.ceil(width));
            }
        }
    }

    @Inject(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), cancellable = true)
    private void onWidthFormattedCharSequence(FormattedCharSequence text, CallbackInfoReturnable<Integer> cir) {
        if (ClientSetting.INSTANCE.replaceMinecraftFont.getValue()) {
            Float width = EpsilonFontMetrics.width(text);
            if (width != null) {
                cir.setReturnValue(Mth.ceil(width));
            }
        }
    }

    @Inject(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), cancellable = true)
    private void onWidthFormattedText(FormattedText text, CallbackInfoReturnable<Integer> cir) {
        if (ClientSetting.INSTANCE.replaceMinecraftFont.getValue()) {
            Float width = EpsilonFontMetrics.width(text);
            if (width != null) {
                cir.setReturnValue(Mth.ceil(width));
            }
        }
    }

    // NameProtect: rewrite the text of the String and Component drawInBatch overloads
    // before they are rendered. Purely client-side; nothing is sent to the server.

    @ModifyVariable(
            method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"), argsOnly = true, index = 1)
    private String epsilon$protectString(String text) {
        if (NameProtect.INSTANCE.shouldProcess()) {
            return NameProtect.INSTANCE.process(text);
        }
        return text;
    }

    @ModifyVariable(
            method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"), argsOnly = true, index = 1)
    private Component epsilon$protectComponent(Component text) {
        if (NameProtect.INSTANCE.shouldProcess()) {
            return NameProtect.INSTANCE.process(text);
        }
        return text;
    }

}

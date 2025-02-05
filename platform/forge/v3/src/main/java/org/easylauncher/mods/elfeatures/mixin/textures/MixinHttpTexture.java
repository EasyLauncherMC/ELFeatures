package org.easylauncher.mods.elfeatures.mixin.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import org.easylauncher.mods.elfeatures.texture.TexturesInspector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.InputStream;

@Mixin(HttpTexture.class)
public abstract class MixinHttpTexture {

    @Unique private int elfeatures$scaleFactor;

    @Inject(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;load(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void computeScaleFactor(InputStream resource, CallbackInfoReturnable<NativeImage> callbackInfo, NativeImage nativeImage) {
        int width = nativeImage.getWidth();
        int height = nativeImage.getHeight();

        int scaleFactor = TexturesInspector.computeTextureScale(width, height);
        if (scaleFactor == 0) {
            callbackInfo.setReturnValue(null);
            callbackInfo.cancel();
        } else {
            this.elfeatures$scaleFactor = scaleFactor;
        }
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;getHeight()I"
            )
    )
    private int adjustHeight(NativeImage instance) {
        return instance.getHeight() / elfeatures$scaleFactor;
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;getWidth()I"
            )
    )
    private int adjustWidth(NativeImage instance) {
        return instance.getWidth() / elfeatures$scaleFactor;
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "NEW",
                    target = "(IIZ)Lcom/mojang/blaze3d/platform/NativeImage;"
            )
    )
    private NativeImage adjustTransformedImageSize(int width, int height, boolean useStbFree) {
        int sf = elfeatures$scaleFactor;
        return new NativeImage(width * sf, height * sf, useStbFree);
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;fillRect(IIIII)V"
            )
    )
    private void adjustFillRectArgs(NativeImage instance, int x, int y, int width, int height, int color) {
        int sf = elfeatures$scaleFactor;
        instance.fillRect(x * sf, y * sf, width * sf, height * sf, color);
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;copyRect(IIIIIIZZ)V"
            )
    )
    private void adjustCopyRectArgs(NativeImage instance, int x0, int y0, int dx, int dy, int width, int height, boolean rx, boolean ry) {
        int sf = elfeatures$scaleFactor;
        instance.copyRect(x0 * sf, y0 * sf, dx * sf, dy * sf, width * sf, height * sf, rx, ry);
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/HttpTexture;setNoAlpha(Lcom/mojang/blaze3d/platform/NativeImage;IIII)V"
            )
    )
    private void adjustSetNoAlphaArgs(NativeImage instance, int x, int y, int width, int height) {
        int sf = elfeatures$scaleFactor;
        m_118022_(instance, x * sf, y * sf, width * sf, height * sf);
    }

    @Redirect(
            method = "Lnet/minecraft/client/renderer/texture/HttpTexture;processLegacySkin(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/HttpTexture;doNotchTransparencyHack(Lcom/mojang/blaze3d/platform/NativeImage;IIII)V"
            )
    )
    private void adjustDoNotchTransparencyHackArgs(NativeImage instance, int x, int y, int width, int height) {
        int sf = elfeatures$scaleFactor;
        m_118012_(instance, x * sf, y * sf, width * sf, height * sf);
    }

    @Shadow private static void m_118022_(NativeImage instance, int x, int y, int width, int height) {}

    @Shadow private static void m_118012_(NativeImage instance, int x, int y, int width, int height) {}

}

package org.easylauncher.mods.elfeatures.mixin.textures;

import net.minecraft.client.render.texture.NativeImage;
import net.minecraft.client.render.texture.SkinImageProcessor;
import org.easylauncher.mods.elfeatures.ELFeaturesAgentBase;
import org.easylauncher.mods.elfeatures.texture.TexturesInspector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.image.BufferedImage;

@Mixin(SkinImageProcessor.class)
public abstract class MixinSkinImageProcessor {

    @Unique private int elfeatures$scaleFactor;

    @Group(name = "head", min = 1, max = 1)
    @Inject(
            method = "process(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void inject_head(BufferedImage image, CallbackInfoReturnable<BufferedImage> callbackInfo) {
        this.elfeatures$scaleFactor = 1;
        if (image == null || ELFeaturesAgentBase.RUNNING_OPTIFINE)
            return;

        int scaleFactor = TexturesInspector.computeTextureScale(image);
        if (scaleFactor == 0) {
            callbackInfo.setReturnValue(image);
            return;
        }

        this.elfeatures$scaleFactor = scaleFactor;
    }

    @Group(name = "head", min = 1, max = 1)
    @Inject(
            method = "process(Lnet/minecraft/client/render/texture/NativeImage;)Lnet/minecraft/client/render/texture/NativeImage;",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void inject_headNative(NativeImage image, CallbackInfoReturnable<NativeImage> callbackInfo) {
        this.elfeatures$scaleFactor = 1;
        if (image == null || ELFeaturesAgentBase.RUNNING_OPTIFINE)
            return;

        int scaleFactor = TexturesInspector.computeTextureScale(image.getWidth(), image.getHeight());
        if (scaleFactor == 0) {
            callbackInfo.setReturnValue(image);
            return;
        }

        this.elfeatures$scaleFactor = scaleFactor;
    }

    @ModifyConstant(
            method = {
                    "process(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
                    "process(Lnet/minecraft/client/render/texture/NativeImage;)Lnet/minecraft/client/render/texture/NativeImage;",
            },
            constant = {
                    @Constant(intValue = -16),  @Constant(intValue = -8),
                    @Constant(intValue = 4),    @Constant(intValue = 8),    @Constant(intValue = 12),
                    @Constant(intValue = 16),   @Constant(intValue = 20),   @Constant(intValue = 24),
                    @Constant(intValue = 28),   @Constant(intValue = 32),   @Constant(intValue = 36),
                    @Constant(intValue = 40),   @Constant(intValue = 44),   @Constant(intValue = 48),
                    @Constant(intValue = 52),   @Constant(intValue = 56),   @Constant(intValue = 64),
            }
    )
    private int modifyConstant_coordinate(int coordinate) {
        return coordinate * elfeatures$scaleFactor;
    }

}

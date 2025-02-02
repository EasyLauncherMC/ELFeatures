package org.easylauncher.mods.elfeatures.mixin.textures;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SkinRemappingImageFilter;
import org.easylauncher.mods.elfeatures.textures.TexturesInspector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(SkinRemappingImageFilter.class)
public abstract class MixinSkinRemappingImageFilter {

    @Unique private int elfeatures$scaleFactor;

    @Inject(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void inject_head(NativeImage nativeImage, CallbackInfoReturnable<NativeImage> callbackInfo) {
        int scaleFactor = TexturesInspector.computeTextureScale(nativeImage.getWidth(), nativeImage.getHeight());
        if (scaleFactor == 0) {
            callbackInfo.setReturnValue(nativeImage);
            callbackInfo.cancel();
            return;
        }

        this.elfeatures$scaleFactor = scaleFactor;
    }

    @ModifyConstant(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            constant = @Constant(intValue = 32, ordinal = 0)
    )
    private int modify_icmpne(int input) {
        return input * elfeatures$scaleFactor;
    }

    @ModifyArgs(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;<init>(IIZ)V"
            )
    )
    private void modifyArgs_NativeImage_init(Args args) {
        for (int i = 0; i <= 1; i++) {
            args.set(i, (int) args.get(i) * elfeatures$scaleFactor);
        }
    }

    @ModifyArgs(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;fillRect(IIIII)V"
            )
    )
    private void modifyArgs_NativeImage_fillRect(Args args) {
        for (int i = 0; i <= 3; i++) {
            args.set(i, (int) args.get(i) * elfeatures$scaleFactor);
        }
    }

    @ModifyArgs(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;copyRect(IIIIIIZZ)V"
            )
    )
    private void modifyArgs_NativeImage_copyRect(Args args) {
        for (int i = 0; i <= 5; i++) {
            args.set(i, (int) args.get(i) * elfeatures$scaleFactor);
        }
    }

    @ModifyArgs(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/SkinRemappingImageFilter;method_3311(Lnet/minecraft/client/texture/NativeImage;IIII)V"
            )
    )
    private void modifyArgs_m3311(Args args) {
        for (int i = 1; i <= 4; i++) {
            args.set(i, (int) args.get(i) * elfeatures$scaleFactor);
        }
    }

    @ModifyArgs(
            method = "filterImage(Lnet/minecraft/client/texture/NativeImage;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/SkinRemappingImageFilter;method_3312(Lnet/minecraft/client/texture/NativeImage;IIII)V"
            )
    )
    private void modifyArgs_m3312(Args args) {
        for (int i = 1; i <= 4; i++) {
            args.set(i, (int) args.get(i) * elfeatures$scaleFactor);
        }
    }

}

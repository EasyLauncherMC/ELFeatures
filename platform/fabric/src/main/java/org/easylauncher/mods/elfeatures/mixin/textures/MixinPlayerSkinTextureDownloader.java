package org.easylauncher.mods.elfeatures.mixin.textures;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import org.easylauncher.mods.elfeatures.texture.TexturesInspector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerSkinTextureDownloader.class)
public abstract class MixinPlayerSkinTextureDownloader {

    @Inject(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void inject_head(
            NativeImage nativeImage,
            String uri,
            CallbackInfoReturnable<NativeImage> callbackInfo,
            @Share("scaleFactor") LocalIntRef scaleFactorRef
    ) {
        int scaleFactor = TexturesInspector.computeTextureScale(nativeImage.getWidth(), nativeImage.getHeight());
        if (scaleFactor == 0) {
            callbackInfo.setReturnValue(nativeImage);
            callbackInfo.cancel();
        } else {
            scaleFactorRef.set(scaleFactor);
        }
    }

    @ModifyConstant(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            constant = { @Constant(intValue = 32), @Constant(intValue = 64) },
            slice = @Slice(from = @At("HEAD"), to = @At(value = "NEW", target = "(IIZ)Lnet/minecraft/client/texture/NativeImage;"))
    )
    private static int modifyConstants(int input, @Share("scaleFactor") LocalIntRef scaleFactorRef) {
        return input * scaleFactorRef.get();
    }

    @ModifyArgs(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;<init>(IIZ)V"
            )
    )
    private static void modifyArgs_NativeImage_init(Args args, @Share("scaleFactor") LocalIntRef scaleFactorRef) {
        for (int i = 0; i <= 1; i++) {
            args.set(i, (int) args.get(i) * scaleFactorRef.get());
        }
    }

    @ModifyArgs(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;fillRect(IIIII)V"
            )
    )
    private static void modifyArgs_NativeImage_fillRect(Args args, @Share("scaleFactor") LocalIntRef scaleFactorRef) {
        for (int i = 0; i <= 3; i++) {
            args.set(i, (int) args.get(i) * scaleFactorRef.get());
        }
    }

    @ModifyArgs(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/NativeImage;copyRect(IIIIIIZZ)V"
            )
    )
    private static void modifyArgs_NativeImage_copyRect(Args args, @Share("scaleFactor") LocalIntRef scaleFactorRef) {
        for (int i = 0; i <= 5; i++) {
            args.set(i, (int) args.get(i) * scaleFactorRef.get());
        }
    }

    @ModifyArgs(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/PlayerSkinTextureDownloader;stripAlpha(Lnet/minecraft/client/texture/NativeImage;IIII)V"
            )
    )
    private static void modifyArgs_stripAlpha(Args args, @Share("scaleFactor") LocalIntRef scaleFactorRef) {
        for (int i = 1; i <= 4; i++) {
            args.set(i, (int) args.get(i) * scaleFactorRef.get());
        }
    }

    @ModifyArgs(
            method = "remapTexture(Lnet/minecraft/client/texture/NativeImage;Ljava/lang/String;)Lnet/minecraft/client/texture/NativeImage;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/texture/PlayerSkinTextureDownloader;stripColor(Lnet/minecraft/client/texture/NativeImage;IIII)V"
            )
    )
    private static void modifyArgs_stripColor(Args args, @Share("scaleFactor") LocalIntRef scaleFactorRef) {
        for (int i = 1; i <= 4; i++) {
            args.set(i, (int) args.get(i) * scaleFactorRef.get());
        }
    }

}

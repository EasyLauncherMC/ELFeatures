package org.easylauncher.mods.elfeatures.mixin.multiplayer;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(
            method = "method_29043()Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_m29043(CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(true);
    }

    @Inject(
            method = "method_29044()Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void inject_m29044(CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(true);
    }

}

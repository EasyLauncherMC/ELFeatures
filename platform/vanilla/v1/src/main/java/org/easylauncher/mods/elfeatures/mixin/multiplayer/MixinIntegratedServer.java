package org.easylauncher.mods.elfeatures.mixin.multiplayer;

import net.minecraft.server.integrated.IntegratedServer;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {

    @ModifyArg(
            method = "init()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/integrated/IntegratedServer;setOnlineMode(Z)V"
            )
    )
    private boolean modifyArg_setOnlineMode(boolean onlineMode) {
        return onlineMode && !ELFeaturesMod.OFFLINE_LAN_ENABLED;
    }

}

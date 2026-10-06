package org.easylauncher.mods.elfeatures.mixin.multiplayer;

import net.minecraft.client.server.IntegratedServer;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IntegratedServer.class)
public abstract class MixinIntegratedServer {

    @ModifyArg(
            method = "initServer()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/server/IntegratedServer;setUsesAuthentication(Z)V"
            )
    )
    private boolean modifyArg_setUsesAuthentication(boolean usesAuthentication) {
        return usesAuthentication && !ELFeaturesMod.OFFLINE_LAN_ENABLED;
    }

}

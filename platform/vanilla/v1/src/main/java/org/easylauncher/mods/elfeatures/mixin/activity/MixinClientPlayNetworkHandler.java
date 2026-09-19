package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import org.easylauncher.mods.elfeatures.activity.LegacyActivityHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The journal entry for every world and server joined, written by {@link LegacyActivityHooks#onJoinGame} once the
 * login packet is handled: a server that turned the player away never gets this far. The handler keeps one
 * intermediary name and descriptor from 1.3 on; the packet is left out of the handler all the same, it is not needed.
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(
            method = "handleLogin",
            at = @At("TAIL"),
            require = 0
    )
    private void inject_handleLogin(CallbackInfo callbackInfo) {
        LegacyActivityHooks.onJoinGame();
    }

}

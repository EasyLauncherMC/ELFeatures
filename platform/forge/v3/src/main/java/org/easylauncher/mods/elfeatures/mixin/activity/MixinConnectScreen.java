package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The address of a connection no server list entry stands behind, as {@code --server} makes, kept for the join it
 * leads to. Up to 1.19.4: from 1.20 on {@code --server} is gone, and quick play makes an entry of its own.
 */
@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen {

    @Inject(
            method = "startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;)V",
            at = @At("HEAD")
    )
    private static void inject_startConnecting(
            Screen parent,
            Minecraft minecraft,
            ServerAddress address,
            ServerData entry,
            CallbackInfo callbackInfo
    ) {
        if (entry == null) {
            ActivityJournalWriter.connecting(address.getHost(), address.getPort());
        }
    }

}

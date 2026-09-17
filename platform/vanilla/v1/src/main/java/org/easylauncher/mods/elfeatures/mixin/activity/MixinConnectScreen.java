package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.multiplayer.ConnectScreen;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The address of a connection no server list entry stands behind, as {@code --server} makes, kept for the join it
 * leads to. This constructor is the one taking a bare host and port; a join from the server list goes through the
 * other one and is named by its entry.
 */
@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen {

    @Inject(
            method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V",
            at = @At("RETURN"),
            require = 0
    )
    private void inject_init(Screen parent, Minecraft minecraft, String host, int port, CallbackInfo callbackInfo) {
        ActivityJournalWriter.connecting(host, port);
    }

}

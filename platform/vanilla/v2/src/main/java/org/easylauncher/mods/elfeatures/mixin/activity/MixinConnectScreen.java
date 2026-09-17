package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ServerAddress;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The address of a connection with no server list entry behind it, kept for the journal entry of the join it leads to.
 *
 * <p>{@code --server} connects that way, so the join finds no entry to name the server by. The host and the port are
 * kept as the game got them, apart and before any SRV lookup. From 23w14a on quick play creates an entry of its own,
 * and nothing is needed there.
 *
 * @see MixinClientPlayNetworkHandler
 */
public final class MixinConnectScreen {

    /** Up to 21w18a: {@code --server} opens the screen through the constructor taking a host and a port. */
    @Mixin(ConnectScreen.class)
    public static abstract class V1 {

        @Inject(
                method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Ljava/lang/String;I)V",
                at = @At("RETURN")
        )
        private void inject_init(Screen parent, MinecraftClient client, String host, int port, CallbackInfo callbackInfo) {
            ActivityJournalWriter.connecting(host, port);
        }

    }

    /**
     * 21w19a – 23w13a: every connection goes through the static {@code connect}, and {@code --server} passes no entry
     * to it.
     */
    @Mixin(ConnectScreen.class)
    public static abstract class V2 {

        @Inject(
                method = "method_36877(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V",
                at = @At("HEAD")
        )
        private static void inject_connect(
                Screen parent,
                MinecraftClient client,
                ServerAddress address,
                ServerInfo entry,
                CallbackInfo callbackInfo
        ) {
            if (entry == null) {
                ActivityJournalWriter.connecting(address.getAddress(), address.getPort());
            }
        }

    }

}

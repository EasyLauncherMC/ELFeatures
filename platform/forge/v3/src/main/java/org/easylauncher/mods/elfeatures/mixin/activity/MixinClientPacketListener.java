package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The journal entry for every world and server joined, written once the login packet is handled.
 *
 * <p>The packet is what the native quick play log waits for too: a server that turned the player away never gets this
 * far, so only the joins that did happen are written. Every join counts, not only the quick ones, and from 1.20 on the
 * native quick play lands here like any other.
 *
 * <p>A running integrated server tells a singleplayer world from a server: the directory comes from the level storage
 * access it holds, reached through {@link MixinMinecraftServer}, the name from its world data. A server is named by its
 * list entry, or, with none, by the address {@link MixinConnectScreen} kept.
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(
            method = "handleLogin",
            at = @At("TAIL")
    )
    private void inject_handleLogin(ClientboundLoginPacket packet, CallbackInfo callbackInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        String gamemode = minecraft.gameMode.getPlayerMode().getName();

        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null) {
            // the server's own access, closed by the server along with the world: only read here, never closed
            @SuppressWarnings("resource")
            LevelStorageSource.LevelStorageAccess storageSource = ((MixinMinecraftServer) server).elfeatures$getStorageSource();
            ActivityJournalWriter.singleplayer(storageSource.getLevelId(), server.getWorldData().getLevelName(), gamemode);
            return;
        }

        ServerData entry = minecraft.getCurrentServer();
        ActivityJournalWriter.multiplayer(entry != null ? entry.ip : null, entry != null ? entry.name : null, gamemode);
    }

}

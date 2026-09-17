package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.LevelProperties;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The journal entry for every world and server joined, written once the game join packet is handled.
 *
 * <p>The packet is what the native quick play log waits for too: a server that turned the player away never gets this
 * far, so only the joins that did happen are written. Every join counts, not only the quick ones, and from 23w14a on
 * the native quick play lands here like any other.
 *
 * <p>A running integrated server tells a singleplayer world from a server. A server is named by its list entry, or,
 * with none, by the address {@link MixinConnectScreen} kept.
 */
public final class MixinClientPlayNetworkHandler {

    /**
     * Up to 20w13b: the server names the world itself, {@code getLevelName()} being its directory and
     * {@code getServerName()} the name it was opened with.
     */
    @Mixin(ClientPlayNetworkHandler.class)
    public static abstract class V1 {

        @Inject(
                method = "onGameJoin",
                at = @At("TAIL")
        )
        private void inject_onGameJoin(GameJoinS2CPacket packet, CallbackInfo callbackInfo) {
            MinecraftClient client = MinecraftClient.getInstance();
            String gamemode = client.interactionManager.getCurrentGameMode().getName();

            IntegratedServer server = client.getServer();
            if (server != null) {
                // the level name is the display name the world was opened with, the one put into level.dat
                ActivityJournalWriter.singleplayer(server.getLevelName(), server.getServerName(), gamemode);
                return;
            }

            ServerInfo entry = client.getCurrentServerEntry();
            ActivityJournalWriter.multiplayer(entry != null ? entry.address : null, entry != null ? entry.name : null, gamemode);
        }

    }

    /**
     * 20w14a – 20w16a: the directory moved into the level storage session the server holds, reached through
     * {@link MixinMinecraftServer.V2}, while {@code getServerName()} still gives the name it was opened with.
     */
    @Mixin(ClientPlayNetworkHandler.class)
    public static abstract class V2 {

        @Inject(
                method = "onGameJoin",
                at = @At("TAIL")
        )
        private void inject_onGameJoin(GameJoinS2CPacket packet, CallbackInfo callbackInfo) {
            MinecraftClient client = MinecraftClient.getInstance();
            String gamemode = client.interactionManager.getCurrentGameMode().getName();

            IntegratedServer server = client.getServer();
            if (server != null) {
                MixinMinecraftServer.V2 accessor = (MixinMinecraftServer.V2) server;
                String directoryName = ((MixinLevelStorageSession) accessor.elfeatures$getSession()).elfeatures$getDirectoryName();
                ActivityJournalWriter.singleplayer(directoryName, server.getServerName(), gamemode);
                return;
            }

            ServerInfo entry = client.getCurrentServerEntry();
            ActivityJournalWriter.multiplayer(entry != null ? entry.address : null, entry != null ? entry.name : null, gamemode);
        }

    }

    /**
     * From 20w17a on: the directory comes from the level storage session the server holds, the name from its save
     * properties. Both are reached through {@link MixinMinecraftServer.V3}.
     */
    @Mixin(ClientPlayNetworkHandler.class)
    public static abstract class V3 {

        @Inject(
                method = "onGameJoin",
                at = @At("TAIL")
        )
        private void inject_onGameJoin(GameJoinS2CPacket packet, CallbackInfo callbackInfo) {
            MinecraftClient client = MinecraftClient.getInstance();
            String gamemode = client.interactionManager.getCurrentGameMode().getName();

            IntegratedServer server = client.getServer();
            if (server != null) {
                MixinMinecraftServer.V3 accessor = (MixinMinecraftServer.V3) server;
                String directoryName = ((MixinLevelStorageSession) accessor.elfeatures$getSession()).elfeatures$getDirectoryName();
                // the one implementation there is, cast to since the interface has no members at build time
                String levelName = ((LevelProperties) accessor.elfeatures$getSaveProperties()).getLevelName();
                ActivityJournalWriter.singleplayer(directoryName, levelName, gamemode);
                return;
            }

            ServerInfo entry = client.getCurrentServerEntry();
            ActivityJournalWriter.multiplayer(entry != null ? entry.address : null, entry != null ? entry.name : null, gamemode);
        }

    }

}

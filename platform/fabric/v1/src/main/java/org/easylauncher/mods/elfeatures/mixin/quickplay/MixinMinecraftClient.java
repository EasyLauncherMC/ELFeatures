package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.activity.QuickPlayWorldHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick play into a singleplayer world before 23w14a, where the game has no {@code --quickPlaySingleplayer} of its own.
 *
 * <p>The world directory comes from {@code elfeatures.quickplay.world} on the first client tick with the title screen
 * open and no overlay on top. The title screen's own {@code init} fires too early twice over: the screen is opened and
 * initialized while resources are still loading, and the splash overlay initializes it once more the moment they are
 * done, before it fades out. Its own {@code tick} is no steadier: it is spelled three ways over these versions, and a
 * few 1.16 snapshots do not declare it at all. The client tick is there all along, under one name.
 *
 * <p>The world is handed out once, so a world that fails to load drops the player back into the main menu rather than
 * into a loop. A world that is not there leaves the game in the main menu with a line in the log.
 *
 * <p>The ways into a world the build version 1.14.4 does not have are shadowed under their intermediary names, left
 * alone by the remapping: {@code remapJar} drops extra member mappings with no official name, so a call to a stub would
 * keep its named spelling at runtime.
 */
public final class MixinMinecraftClient {

    /**
     * 18w47b – 19w07a: {@code startIntegratedServer(String, String, LevelInfo)} with no settings, as the world list
     * itself calls it. The second argument is written into {@code level.dat} over the name already there, so it is
     * taken from the world list first; no entry there means no world.
     *
     * <p>No overlay yet: the title screen is opened only once resources are loaded, at the end of startup or, by
     * 19w07a, by the splash screen itself.
     */
    @Mixin(MinecraftClient.class)
    public static abstract class V1 {

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            if (!(client.currentScreen instanceof TitleScreen))
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null)
                return;

            // the name goes to level.dat as is, so it has to be the one already there
            LevelSummary summary = null;
            try {
                for (LevelSummary level : ((MixinLevelStorage) client.getLevelStorage()).elfeatures$getLevelList()) {
                    if (level.getName().equals(world)) {
                        summary = level;
                    }
                }
            } catch (LevelStorageException ignored) {
                // no saves directory to list, so no world either
            }

            if (summary == null) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            client.startIntegratedServer(world, summary.getDisplayName(), null);
        }

    }

    /**
     * 19w08a – 20w16a: the same start as {@link V1}, now with the splash overlay to wait for.
     */
    @Mixin(MinecraftClient.class)
    public static abstract class V2 {

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            if (!(client.currentScreen instanceof TitleScreen) || client.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null)
                return;

            // the name goes to level.dat as is, so it has to be the one already there
            LevelSummary summary = null;
            try {
                for (LevelSummary level : ((MixinLevelStorage) client.getLevelStorage()).elfeatures$getLevelList()) {
                    if (level.getName().equals(world)) {
                        summary = level;
                    }
                }
            } catch (LevelStorageException ignored) {
                // no saves directory to list, so no world either
            }

            if (summary == null) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            client.startIntegratedServer(world, summary.getDisplayName(), null);
        }

    }

    /**
     * 20w17a – 20w22a: {@code startIntegratedServer(String, LevelInfo)} no longer takes a name, and no settings open the
     * world as it is, as the world list calls it.
     */
    @Mixin(MinecraftClient.class)
    public static abstract class V3 {

        @Shadow(remap = false)
        public abstract void method_1559(String worldName, LevelInfo levelInfo);

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            if (!(client.currentScreen instanceof TitleScreen) || client.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null)
                return;

            if (!client.getLevelStorage().levelExists(world)) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            method_1559(world, null);
        }

    }

    /**
     * 1.16-pre1 – 1.18.2: {@code startIntegratedServer(String)} takes the directory alone and reads everything else
     * from the world, so only its presence is checked.
     */
    @Mixin(MinecraftClient.class)
    public static abstract class V4 {

        @Shadow(remap = false)
        public abstract void method_29606(String worldName);

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            if (!(client.currentScreen instanceof TitleScreen) || client.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null)
                return;

            if (!client.getLevelStorage().levelExists(world)) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            method_29606(world);
        }

    }

    /**
     * 22w11a: the start moved into {@code IntegratedServerLoader}, which takes the directory alone for now and shows
     * whatever it has to on the way with no parent screen.
     */
    @Mixin(MinecraftClient.class)
    public static abstract class V5 {

        @Shadow(remap = false)
        public abstract IntegratedServerLoader method_41735();

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            if (!(client.currentScreen instanceof TitleScreen) || client.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null)
                return;

            if (!client.getLevelStorage().levelExists(world)) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            ((MixinIntegratedServerLoader.V5) method_41735()).elfeatures$start(world);
        }

    }

    /**
     * 22w12a – 23w13a: {@code IntegratedServerLoader} takes a parent screen for whatever it shows on the way too; the
     * title screen is that parent.
     */
    @Mixin(MinecraftClient.class)
    public static abstract class V6 {

        @Shadow(remap = false)
        public abstract IntegratedServerLoader method_41735();

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            Screen screen = client.currentScreen;
            if (!(screen instanceof TitleScreen) || client.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null)
                return;

            if (!client.getLevelStorage().levelExists(world)) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            ((MixinIntegratedServerLoader.V6) method_41735()).elfeatures$start(screen, world);
        }

    }

}

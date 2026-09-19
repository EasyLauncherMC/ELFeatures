package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.activity.QuickPlayWorldHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick play into a singleplayer world before 1.20, where the game has no {@code --quickPlaySingleplayer} of its own.
 *
 * <p>The world directory comes from {@code elfeatures.quickplay.world} on the first client tick with the title screen
 * open and no overlay on top. The title screen's own {@code init} fires too early twice over: the screen is opened and
 * initialized while resources are still loading, and the loading overlay initializes it once more the moment they are
 * done, before it fades out.
 *
 * <p>The world is handed out once, so a world that fails to load drops the player back into the main menu rather than
 * into a loop. A world that is not there leaves the game in the main menu with a line in the log.
 */
public final class MixinMinecraft {

    /**
     * 1.17.1 – 1.18.2: {@code loadLevel(String)} takes the directory alone and reads everything else from the world,
     * so only its presence is checked.
     */
    @Mixin(Minecraft.class)
    public static abstract class V1 {

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            Minecraft minecraft = (Minecraft) (Object) this;
            if (!(minecraft.screen instanceof TitleScreen) || minecraft.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null) return;

            if (!minecraft.getLevelSource().levelExists(world)) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            minecraft.loadLevel(world);
        }

    }

    /**
     * 1.19 – 1.19.4: the start moved into {@code WorldOpenFlows}, which takes a parent screen for whatever it shows on
     * the way; the title screen is that parent.
     *
     * <p>Neither exists in the build version 1.17.1, so the renaming to SRG knows nothing of them: both are named by
     * their SRG names outright, {@code createWorldOpenFlows()} shadowed here and {@code loadLevel} invoked through
     * {@link MixinWorldOpenFlows}.
     */
    @Mixin(Minecraft.class)
    public static abstract class V2 {

        @Shadow(remap = false)
        public abstract WorldOpenFlows m_231466_();

        @Inject(
                method = "tick()V",
                at = @At("HEAD")
        )
        private void inject_tick(CallbackInfo callbackInfo) {
            Minecraft minecraft = (Minecraft) (Object) this;
            Screen screen = minecraft.screen;
            if (!(screen instanceof TitleScreen) || minecraft.getOverlay() != null)
                return;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world == null) return;

            if (!minecraft.getLevelSource().levelExists(world)) {
                ELFeaturesMod.LOGGER.warn("Quick play world '{}' not found, staying in the main menu", world);
                return;
            }

            ((MixinWorldOpenFlows) m_231466_()).elfeatures$loadLevel(screen, world);
        }

    }

}

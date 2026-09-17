package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.MinecraftClient;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The idle entry that closes a join, written on the first client tick with no world.
 *
 * <p>The world is dropped whichever way the player leaves it, the pause menu, a kick or the death screen, while the
 * ways out change names over these versions; the tick and the world field keep one intermediary name all along. The
 * writer drops the entry while no join is open, so the ticks in the menus write nothing.
 *
 * @see MixinClientPlayNetworkHandler
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(
            method = "tick()V",
            at = @At("HEAD")
    )
    private void inject_tick(CallbackInfo callbackInfo) {
        if (((MinecraftClient) (Object) this).world == null)
            ActivityJournalWriter.idle();
    }

}

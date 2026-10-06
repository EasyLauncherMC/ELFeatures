package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.Minecraft;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The idle entry that closes a join, written on the first client tick with no level.
 *
 * <p>The level is dropped whichever way the player leaves it, the pause menu, a kick, the death screen or a transfer to
 * another server, while the ways out change names and signatures from one version to the next; the tick and the level
 * field stay put. The writer drops the entry while no join is open, so the ticks in the menus write nothing.
 *
 * @see MixinClientPacketListener
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(
            method = "tick()V",
            at = @At("HEAD")
    )
    private void inject_tick(CallbackInfo callbackInfo) {
        if (((Minecraft) (Object) this).level == null) {
            ActivityJournalWriter.idle();
        }
    }

}

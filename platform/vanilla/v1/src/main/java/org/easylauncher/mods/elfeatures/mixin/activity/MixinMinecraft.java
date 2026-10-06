package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldSettings;
import org.easylauncher.mods.elfeatures.activity.LegacyActivityHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The journal entry for a world of one's own up to 1.2.5, which is joined with no login packet to hook: written by
 * {@link LegacyActivityHooks#onStartGame} once the world is open.
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(
            method = "startGame(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void inject_startGame(String directoryName, String levelName, WorldSettings settings, CallbackInfo callbackInfo) {
        LegacyActivityHooks.onStartGame(this, directoryName);
    }

}

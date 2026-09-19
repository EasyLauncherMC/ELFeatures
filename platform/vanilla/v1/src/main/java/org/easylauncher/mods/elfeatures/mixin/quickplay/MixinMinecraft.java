package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.client.Minecraft;
import org.easylauncher.mods.elfeatures.activity.LegacyActivityHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick play before 23w14a and the idle entry: every client tick is handed to {@link LegacyActivityHooks#onClientTick},
 * which closes a join once the world is gone and opens the quick play world once the title screen is up. The tick itself
 * keeps one intermediary name from 1.0 on.
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(
            method = "tick()V",
            at = @At("HEAD"),
            require = 0
    )
    private void inject_tick(CallbackInfo callbackInfo) {
        LegacyActivityHooks.onClientTick(this);
    }

}

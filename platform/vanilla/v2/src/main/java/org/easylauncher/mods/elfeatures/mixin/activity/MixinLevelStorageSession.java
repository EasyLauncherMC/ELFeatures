package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.world.level.storage.LevelStorage$Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * The directory of the world a level storage session is open on, from 20w14a on.
 *
 * <p>The session type is a stub the build version 1.14.4 does not have, so its method is invoked by the intermediary
 * name rather than called on the stub.
 */
@Mixin(LevelStorage$Session.class)
public interface MixinLevelStorageSession {

    @Invoker("method_27005")
    String elfeatures$getDirectoryName();

}

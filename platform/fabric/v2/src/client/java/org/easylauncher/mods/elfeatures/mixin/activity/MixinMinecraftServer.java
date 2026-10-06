package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The level storage access the integrated server runs its world on, a protected field with no getter.
 */
@Mixin(MinecraftServer.class)
public interface MixinMinecraftServer {

    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess elfeatures$getStorageSource();

}

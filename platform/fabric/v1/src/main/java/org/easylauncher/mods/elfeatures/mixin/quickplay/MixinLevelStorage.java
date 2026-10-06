package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * The world list, where quick play takes the name of a world from up to 20w16a.
 *
 * <p>Invoked rather than called: the level storage is a class in the build version 1.14.4 but an interface in the
 * 1.14 snapshots up to 19w02a at least, and a direct call compiled for the class fails on the interface. The invoker is generated
 * into whichever of the two the game has.
 */
@Mixin(LevelStorage.class)
public interface MixinLevelStorage {

    @Invoker("getLevelList")
    List<LevelSummary> elfeatures$getLevelList() throws LevelStorageException;

}

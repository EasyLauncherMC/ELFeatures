package org.easylauncher.mods.elfeatures.mixin.activity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage$Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * What the integrated server knows about the world it runs, from 20w14a on.
 *
 * <p>The build version 1.14.4 has neither the session field nor the save properties, and both types are stubs, so the
 * members are reached through accessors and invokers with intermediary names.
 */
public final class MixinMinecraftServer {

    /** 20w14a – 20w16a: the level storage session alone, the name is still the server's own. */
    @Mixin(MinecraftServer.class)
    public interface V2 {

        @Accessor("field_23784")
        LevelStorage$Session elfeatures$getSession();

    }

    /** From 20w17a on: the session and the save properties, which took the name over. */
    @Mixin(MinecraftServer.class)
    public interface V3 {

        @Accessor("field_23784")
        LevelStorage$Session elfeatures$getSession();

        @Invoker("method_27728")
        SaveProperties elfeatures$getSaveProperties();

    }

}

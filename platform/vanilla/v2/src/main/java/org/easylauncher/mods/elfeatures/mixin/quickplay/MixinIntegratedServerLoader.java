package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServerLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code IntegratedServerLoader.start}, the way into a world from 22w11a on.
 *
 * <p>The build version 1.14.4 has no such class, so it is a stub. Its method is invoked rather than called on the
 * stub: {@code remapJar} drops extra member mappings with no official name, so a direct call would keep its named
 * spelling at runtime.
 */
public final class MixinIntegratedServerLoader {

    /** 22w11a: {@code start(String)}. */
    @Mixin(IntegratedServerLoader.class)
    public interface V5 {

        @Invoker("method_41894")
        void elfeatures$start(String levelName);

    }

    /** 22w12a – 23w13a: {@code start(Screen, String)}. */
    @Mixin(IntegratedServerLoader.class)
    public interface V6 {

        @Invoker("method_41894")
        void elfeatures$start(Screen parent, String levelName);

    }

}

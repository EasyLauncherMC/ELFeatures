package org.easylauncher.mods.elfeatures.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.easylauncher.mods.elfeatures.loader.service.MixinService;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Running the mixins over a class, with nothing in it about how the class was handed over.
 *
 * <p>Both ways into a game end here: the agent, which the JVM hands every class it is about to define, and the
 * tweaker, which a launch wrapper hands the same classes a step earlier.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MixinPipeline {

    /**
     * Whether the class is the game's, which is the package it is in: {@code net.minecraft} where the release
     * is named, and the default package where it is obfuscated.
     *
     * <p>A class that isn't a target is handed straight back, so this keeps the JDK and the libraries out and
     * does nothing more.
     */
    public static boolean isMinecraftClass(String internalName) {
        return internalName != null && (!internalName.contains("/") || internalName.startsWith("net/minecraft/"));
    }

    /** The class with the mixins in it, or {@code null} where none of them had anything to say about it. */
    public static byte[] apply(String internalName, byte[] classBytes) {
        MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        String name = internalName.replace('/', '.');

        byte[] transformed = MixinService.getTransformer().transformClass(environment, name, classBytes);
        return transformed == classBytes ? null : transformed;
    }

    /**
     * The class mixin makes up as it works — the argument holder of a {@code @ModifyArgs} and its like — or
     * {@code null} where the name is not one of mixin's.
     */
    public static byte[] generate(String internalName) {
        MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        return MixinService.getTransformer().generateClass(environment, internalName.replace('/', '.'));
    }

}

package org.easylauncher.mods.elfeatures.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.easylauncher.mods.elfeatures.loader.naming.MixinClassRemapper;
import org.easylauncher.mods.elfeatures.loader.service.MixinService;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Running the mixins over a class, with nothing in it about how the class was handed over.
 *
 * <p>Both ways into a game end here: the agent, which the JVM hands every class it is about to define, and the
 * tweaker, which a launch wrapper hands the same classes a step earlier.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MixinPipeline {

    private static final String MIXIN_PACKAGE = "org/easylauncher/mods/elfeatures/mixin/";
    private static final MixinClassRemapper MIXIN_REMAPPER = new MixinClassRemapper();

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

    /**
     * Whether the class is one of the mod's mixins. The JVM ever loads one itself only where it is an accessor:
     * an interface the game's classes are made to implement, and the handlers cast to.
     */
    public static boolean isMixinClass(String internalName) {
        return internalName != null && internalName.startsWith(MIXIN_PACKAGE);
    }

    /**
     * The accessor rewritten into the names the running game has, or {@code null} where it runs on the names the
     * mixins are written against.
     *
     * <p>Mixin reads a mixin through the service, which rewrites it on the way, and generates the accessor methods
     * from that copy. The interface the JVM loads comes from the jar as it is: left alone, it would still declare
     * those methods with intermediary types, and no call to them would find the ones generated.
     */
    public static byte[] remapAccessor(byte[] classBytes) {
        if (ELFeaturesMixinBootstrap.getMixinRemapper() == null)
            return null;

        ClassWriter writer = new ClassWriter(0);
        new ClassReader(classBytes).accept(new ClassRemapper(writer, MIXIN_REMAPPER), 0);
        return writer.toByteArray();
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

package org.easylauncher.mods.elfeatures.loader;

import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.loader.mixin.MixinService;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Runs the mixins over every game class the JVM is about to define.
 *
 * <p>A class counts as the game's by the package it is in: {@code net.minecraft} where the release is named,
 * and the default package where it is obfuscated. What isn't a target mixin hands straight back, so the filter
 * is there to keep the JDK and the libraries out and nothing more.
 */
@Log4j2
public final class ELFeaturesClassTransformer implements ClassFileTransformer {

    private final SyntheticClasses syntheticClasses;

    public ELFeaturesClassTransformer(Instrumentation instrumentation) {
        this.syntheticClasses = new SyntheticClasses(instrumentation);
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        if (className == null || classfileBuffer == null || classfileBuffer.length == 0)
            return null;

        if (!isMinecraftClass(className))
            return null;

        try {
            MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
            String name = className.replace('/', '.');

            byte[] transformed = MixinService.getTransformer().transformClass(environment, name, classfileBuffer);
            if (transformed == classfileBuffer)
                return null;

            syntheticClasses.defineReferencedBy(transformed, environment);
            return transformed;
        } catch (Throwable cause) {
            log.error("Couldn't apply the mixins to '" + className + "'!", cause);
            return null;
        }
    }

    public static boolean isMinecraftClass(String internalName) {
        return internalName != null && (!internalName.contains("/") || internalName.startsWith("net/minecraft/"));
    }

}

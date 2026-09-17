package org.easylauncher.mods.elfeatures.loader;

import lombok.extern.log4j.Log4j2;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/** Runs the mixins over every game class the JVM is about to define. */
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

        if (MixinPipeline.isMixinClass(className)) {
            try {
                return MixinPipeline.remapAccessor(classfileBuffer);
            } catch (Throwable cause) {
                log.error("Couldn't rename the accessor '" + className + "'!", cause);
                return null;
            }
        }

        if (!MixinPipeline.isMinecraftClass(className))
            return null;

        try {
            byte[] transformed = MixinPipeline.apply(className, classfileBuffer);
            if (transformed == null) return null;

            syntheticClasses.defineReferencedBy(transformed);
            return transformed;
        } catch (Throwable cause) {
            log.error("Couldn't apply the mixins to '" + className + "'!", cause);
            return null;
        }
    }

}

package org.easylauncher.mods.elfeatures.loader;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.ELFeaturesLoaderModBase;
import org.easylauncher.mods.elfeatures.loader.service.MixinService;

/**
 * The mod as a launch wrapper transformer, brought up by the wrapper's own loader.
 *
 * <p>Everything happens in the constructor because of where it runs: the wrapper instantiates a registered
 * transformer inside its class loader, which is the one loader the mod may live in — and the only place from
 * which mixin can be pointed at the classes the game will actually be given.
 */
public abstract class ELFeaturesLaunchTransformerBase extends ELFeaturesLoaderModBase implements IClassTransformer {

    private static final Logger LOG = LogManager.getLogger(ELFeaturesLaunchTransformerBase.class);

    protected ELFeaturesLaunchTransformerBase(String modVersion, Logger logger) {
        super(modVersion, logger);

        MixinService.setClassSource(Launch.classLoader);
        startMixin();

        log("[ELFeaturesTweaker] Hung on the game, transforming from here on");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBytes) {
        String internalName = transformedName.replace('.', '/');
        if (classBytes == null) return generated(internalName);

        if (!MixinPipeline.isMinecraftClass(internalName))
            return classBytes;

        try {
            byte[] transformed = MixinPipeline.apply(internalName, classBytes);
            return transformed != null ? transformed : classBytes;
        } catch (Throwable cause) {
            LOG.error("Couldn't apply the mixins to '" + transformedName + "'!", cause);
            return classBytes;
        }
    }

    private byte[] generated(String internalName) {
        try {
            return MixinPipeline.generate(internalName);
        } catch (Throwable cause) {
            LOG.error("Couldn't make up the class '" + internalName + "'!", cause);
            return null;
        }
    }

}

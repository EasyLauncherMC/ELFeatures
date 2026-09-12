package org.easylauncher.mods.elfeatures;

import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * The mod in a game with no mod loader in it, which is what both ways in have in common.
 *
 * <p>What differs is only where the bytes come from: the JVM hands them to an agent, a launch wrapper hands the
 * same ones to a tweaker a step earlier.
 */
public abstract class ELFeaturesLoaderModBase extends ELFeaturesModBase {

    public static final boolean RUNNING_OPTIFINE;

    protected ELFeaturesLoaderModBase(String modVersion, Logger logger) {
        super(String.format("ELFeatures/%s (%s)", modVersion, RUNNING_OPTIFINE ? "OptiFine" : "Vanilla"), logger);
    }

    protected final void startMixin() {
        ELFeaturesMixinBootstrap.init();

        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    static {
        RUNNING_OPTIFINE = "true".equalsIgnoreCase(System.getProperty("elfeatures.running.optifine"));
    }

}

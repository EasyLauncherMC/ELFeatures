package org.easylauncher.mods.elfeatures;

import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesClassTransformer;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.instrument.Instrumentation;

/**
 * The mod as a plain java agent: no mod loader, no launch wrapper, just {@code premain} in front of the game's
 * own {@code main}.
 *
 * <p>Mixin is brought up here and the transformer hung on {@link Instrumentation}, so what a mixin sees are the
 * bytes the JVM is actually about to define — already patched by OptiFine where OptiFine is installed, rather
 * than the untouched ones lying in the client jar.
 */
public abstract class ELFeaturesAgentBase extends ELFeaturesModBase {

    public static final boolean RUNNING_OPTIFINE;

    protected ELFeaturesAgentBase(String modVersion, Logger logger) {
        super(String.format("ELFeatures/%s (%s)", modVersion, RUNNING_OPTIFINE ? "OptiFine" : "Vanilla"), logger);
    }

    /**
     * Brings mixin up and starts transforming.
     *
     * <p>The transformer goes on last on purpose: everything mixin loads while starting would otherwise run
     * through a transformer whose remapper isn't there yet.
     */
    protected final void start(Instrumentation instrumentation) {
        ELFeaturesMixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);

        instrumentation.addTransformer(new ELFeaturesClassTransformer(instrumentation));
        log("[ELFeaturesAgent] Hung on the game, transforming from here on");
    }

    static {
        RUNNING_OPTIFINE = "true".equalsIgnoreCase(System.getProperty("elfeatures.running.optifine"));
    }

}

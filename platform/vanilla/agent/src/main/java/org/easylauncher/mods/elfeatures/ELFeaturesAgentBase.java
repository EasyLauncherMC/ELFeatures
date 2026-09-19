package org.easylauncher.mods.elfeatures;

import lombok.CustomLog;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesClassTransformer;

import java.lang.instrument.Instrumentation;

/**
 * The mod as a plain java agent: no mod loader, no launch wrapper, just {@code premain} in front of the game's
 * own {@code main}.
 *
 * <p>The transformer hangs on {@link Instrumentation}, so what a mixin sees are the bytes the JVM is actually
 * about to define — already patched by OptiFine where OptiFine is installed, rather than the untouched ones
 * lying in the client jar.
 */
@CustomLog
public abstract class ELFeaturesAgentBase extends ELFeaturesLoaderModBase {

    protected ELFeaturesAgentBase(String modVersion) {
        super(modVersion);
    }

    /** Brings mixin up and starts transforming. */
    protected final void start(Instrumentation instrumentation) {
        startMixin();

        instrumentation.addTransformer(new ELFeaturesClassTransformer(instrumentation));
        log.info("[ELFeaturesAgent] Hung on the game, transforming from here on");
    }

}

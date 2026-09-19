package org.easylauncher.mods.elfeatures;

import java.lang.instrument.Instrumentation;

public final class ELFeaturesAgent extends ELFeaturesAgentBase {

    private ELFeaturesAgent() {
        super(Constants.MOD_VERSION);
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        // a copy of the mod brought up here would stay outside the wrapper's loader, where the game is
        if (VanillaTweakerTransformer.isGameWrapped()) {
            instrumentation.addTransformer(new VanillaTweakerTransformer());
            return;
        }

        new ELFeaturesAgent().start(instrumentation);
    }

}

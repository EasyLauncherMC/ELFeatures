package org.easylauncher.mods.elfeatures;

import java.lang.instrument.Instrumentation;

public final class ELFeaturesAgent extends ELFeaturesAgentBase {

    private ELFeaturesAgent() {
        super(Constants.MOD_VERSION);
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        new ELFeaturesAgent().start(instrumentation);
    }

}

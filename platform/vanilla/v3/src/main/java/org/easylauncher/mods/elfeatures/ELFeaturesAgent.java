package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;

import java.lang.instrument.Instrumentation;

@Log4j2
public final class ELFeaturesAgent extends ELFeaturesAgentBase {

    private ELFeaturesAgent() {
        super(Constants.MOD_VERSION, log);
    }

    public static void premain(String arguments, Instrumentation instrumentation) {
        new ELFeaturesAgent().start(instrumentation);
    }

}

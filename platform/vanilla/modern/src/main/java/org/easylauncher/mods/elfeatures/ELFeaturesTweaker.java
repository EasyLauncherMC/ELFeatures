package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ELFeaturesTweaker extends ELFeaturesTweakerBase {

    public ELFeaturesTweaker() {
        super(Constants.MOD_VERSION, log);
    }

    @Override
    public String getModName() {
        return Constants.MOD_NAME;
    }

}

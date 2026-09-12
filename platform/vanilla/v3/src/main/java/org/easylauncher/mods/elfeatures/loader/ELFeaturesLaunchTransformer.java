package org.easylauncher.mods.elfeatures.loader;

import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.Constants;

@Log4j2
public final class ELFeaturesLaunchTransformer extends ELFeaturesLaunchTransformerBase {

    public ELFeaturesLaunchTransformer() {
        super(Constants.MOD_VERSION, log);
    }

}

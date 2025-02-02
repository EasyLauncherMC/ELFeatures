package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.textures.TexturesProvider;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

public interface ELFeaturesMod extends LoggingFacade {

    TexturesProvider getTexturesProvider();

    static ELFeaturesMod mod() {
        return ELFeaturesService.getInstance().getMod();
    }

    static TexturesProvider texturesProvider() {
        return mod().getTexturesProvider();
    }

}

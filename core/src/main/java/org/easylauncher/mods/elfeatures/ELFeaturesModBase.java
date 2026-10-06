package org.easylauncher.mods.elfeatures;

import lombok.Getter;
import org.easylauncher.mods.elfeatures.texture.provider.TexturesProviderService;

@Getter
abstract class ELFeaturesModBase implements ELFeaturesMod {

    private final TexturesProviderService texturesProviderService;

    ELFeaturesModBase(String userAgent) {
        this.texturesProviderService = new TexturesProviderService(userAgent);
        ELFeaturesService.initialize(this);
    }

}

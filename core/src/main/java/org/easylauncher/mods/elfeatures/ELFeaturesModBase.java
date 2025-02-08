package org.easylauncher.mods.elfeatures;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.texture.provider.TexturesProviderService;

@Getter
abstract class ELFeaturesModBase implements ELFeaturesMod {

    @Getter(AccessLevel.NONE)
    private final Logger logger;
    private final TexturesProviderService texturesProviderService;

    ELFeaturesModBase(String userAgent, Logger logger) {
        this.logger = logger;
        this.texturesProviderService = new TexturesProviderService(userAgent, this);
        ELFeaturesService.initialize(this);
    }

    @Override
    public void log(String message, Object... args) {
        if (LOGGING_ENABLED) {
            logger.info(String.format(message, args));
        }
    }

}

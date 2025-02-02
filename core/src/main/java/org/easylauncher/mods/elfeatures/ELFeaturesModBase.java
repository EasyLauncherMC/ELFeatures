package org.easylauncher.mods.elfeatures;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.textures.TexturesProvider;

@Getter
abstract class ELFeaturesModBase implements ELFeaturesMod {

    public static final boolean DEBUG_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.debug.enabled"));
    public static final boolean LOGGING_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.logging.enabled"));

    @Getter(AccessLevel.NONE)
    private final Logger logger;
    private final TexturesProvider texturesProvider;

    ELFeaturesModBase(String userAgent, Logger logger) {
        this.logger = logger;
        this.texturesProvider = new TexturesProvider(userAgent, this);
        ELFeaturesService.initialize(this);
    }

    @Override
    public void log(String message, Object... args) {
        if (LOGGING_ENABLED) {
            logger.info(String.format(message, args));
        }
    }

}

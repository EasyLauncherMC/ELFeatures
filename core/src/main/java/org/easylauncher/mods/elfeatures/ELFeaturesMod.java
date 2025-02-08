package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.texture.provider.*;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

public interface ELFeaturesMod extends LoggingFacade {

    boolean DEBUG_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.debug.enabled"));
    boolean LOGGING_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.logging.enabled"));

    TexturesProviderService getTexturesProviderService();

    static ELFeaturesMod mod() {
        return ELFeaturesService.getInstance().getMod();
    }

    static TexturesProviderService texturesProviderService() {
        return mod().getTexturesProviderService();
    }

    static AuthlibEasyxTexturesProvider authlibEasyxTexturesProvider() {
        return texturesProviderService().authlibEasyxTexturesProvider();
    }

    static LegacyEasyxTexturesProvider legacyEasyxTexturesProvider() {
        return texturesProviderService().legacyEasyxTexturesProvider();
    }

    static LegacyMojangTexturesProvider legacyMojangTexturesProvider() {
        return texturesProviderService().legacyMojangTexturesProvider();
    }

}

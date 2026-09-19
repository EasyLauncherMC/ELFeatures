package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.logging.LoggerAdapter;
import org.easylauncher.mods.elfeatures.texture.provider.*;

public interface ELFeaturesMod {

    boolean DEBUG_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.debug.enabled"));
    boolean LEGACY_SKINS_ONLY = "true".equalsIgnoreCase(System.getProperty("elfeatures.skins.legacy"));
    boolean LOGGING_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.logging.enabled"));
    boolean OFFLINE_LAN_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.lan.offline"));

    /** For mixins: a static field of their own would end up in the game's class, and its initializer with it. */
    LoggerAdapter LOGGER = LoggerAdapter.of(ELFeaturesMod.class);

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

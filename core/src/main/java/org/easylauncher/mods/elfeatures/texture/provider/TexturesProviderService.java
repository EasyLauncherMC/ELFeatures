package org.easylauncher.mods.elfeatures.texture.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

@RequiredArgsConstructor
public final class TexturesProviderService {

    @Getter private final String userAgent;
    @Getter private final LoggingFacade logger;

    private AuthlibEasyxTexturesProvider authlibEasyxTexturesProvider;
    private LegacyEasyxTexturesProvider legacyEasyxTexturesProvider;
    private LegacyMojangTexturesProvider legacyMojangTexturesProvider;

    public synchronized AuthlibEasyxTexturesProvider authlibEasyxTexturesProvider() {
        if (authlibEasyxTexturesProvider == null)
            this.authlibEasyxTexturesProvider = new AuthlibEasyxTexturesProvider(userAgent, logger);

        return authlibEasyxTexturesProvider;
    }

    public synchronized LegacyEasyxTexturesProvider legacyEasyxTexturesProvider() {
        if (legacyEasyxTexturesProvider == null)
            this.legacyEasyxTexturesProvider = new LegacyEasyxTexturesProvider(userAgent, logger);

        return legacyEasyxTexturesProvider;
    }

    public synchronized LegacyMojangTexturesProvider legacyMojangTexturesProvider() {
        if (legacyMojangTexturesProvider == null)
            this.legacyMojangTexturesProvider = new LegacyMojangTexturesProvider(userAgent, logger);

        return legacyMojangTexturesProvider;
    }

}

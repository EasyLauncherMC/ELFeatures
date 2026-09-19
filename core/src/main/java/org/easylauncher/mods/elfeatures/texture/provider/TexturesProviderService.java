package org.easylauncher.mods.elfeatures.texture.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TexturesProviderService {

    @Getter private final String userAgent;

    private AuthlibEasyxTexturesProvider authlibEasyxTexturesProvider;
    private LegacyEasyxTexturesProvider legacyEasyxTexturesProvider;
    private LegacyMojangTexturesProvider legacyMojangTexturesProvider;

    public synchronized AuthlibEasyxTexturesProvider authlibEasyxTexturesProvider() {
        if (authlibEasyxTexturesProvider == null)
            this.authlibEasyxTexturesProvider = new AuthlibEasyxTexturesProvider(userAgent);

        return authlibEasyxTexturesProvider;
    }

    public synchronized LegacyEasyxTexturesProvider legacyEasyxTexturesProvider() {
        if (legacyEasyxTexturesProvider == null)
            this.legacyEasyxTexturesProvider = new LegacyEasyxTexturesProvider(userAgent);

        return legacyEasyxTexturesProvider;
    }

    public synchronized LegacyMojangTexturesProvider legacyMojangTexturesProvider() {
        if (legacyMojangTexturesProvider == null)
            this.legacyMojangTexturesProvider = new LegacyMojangTexturesProvider(userAgent);

        return legacyMojangTexturesProvider;
    }

}

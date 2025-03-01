package org.easylauncher.mods.elfeatures.texture.provider;

import com.mojang.authlib.GameProfile;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;
import org.easylauncher.mods.elfeatures.util.UsernameValidator;

public final class LegacyEasyxTexturesProvider extends LegacyTexturesProviderBase<String> {

    public LegacyEasyxTexturesProvider(String userAgent, LoggingFacade logger) {
        super(userAgent, logger);
    }

    @Override
    protected String keyFromProfile(GameProfile profile) {
        return profile != null ? profile.getName() : null;
    }

    @Override
    protected String formatTexturesUrl(String username) {
        return String.format(EASYX_TEXTURES_URL_PATTERN, username.toLowerCase());
    }

    @Override
    protected boolean validateKey(String username) {
        return UsernameValidator.isValidUsername(username);
    }

}

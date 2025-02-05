package org.easylauncher.mods.elfeatures.texture.provider;

import com.mojang.authlib.GameProfile;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProperty;
import org.easylauncher.mods.elfeatures.texture.model.LegacyTexturesPayload;
import org.easylauncher.mods.elfeatures.texture.model.LegacyTexturesResponse;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class LegacyMojangTexturesProvider extends LegacyTexturesProviderBase<UUID> {

    public LegacyMojangTexturesProvider(String userAgent, LoggingFacade logger) {
        super(userAgent, logger);
    }

    @Override
    protected UUID keyFromProfile(GameProfile profile) {
        return profile != null ? profile.getId() : null;
    }

    @Override
    protected String formatTexturesUrl(UUID uuid) {
        return String.format(MOJANG_TEXTURES_URL_PATTERN, uuid);
    }

    @Override
    public LegacyTexturesPayload parseTexturesPayload(byte[] rawResponseBody) {
        String jsonString = new String(rawResponseBody, StandardCharsets.UTF_8);
        LegacyTexturesResponse response = gson.fromJson(jsonString, LegacyTexturesResponse.class);

        List<LegacyProperty> properties = response.getProperties();
        if (properties != null && !properties.isEmpty()) {
            Optional<String> texturesValue = properties.stream()
                    .filter(property -> "textures".equalsIgnoreCase(property.getName()))
                    .findFirst()
                    .map(LegacyProperty::getValue);

            if (texturesValue.isPresent()) {
                rawResponseBody = Base64.getDecoder().decode(texturesValue.get());
                return super.parseTexturesPayload(rawResponseBody);
            }
        }

        return new LegacyTexturesPayload(response.getId(), response.getName(), new HashMap<>());
    }

}

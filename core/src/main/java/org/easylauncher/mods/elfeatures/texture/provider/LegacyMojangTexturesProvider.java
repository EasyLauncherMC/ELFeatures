package org.easylauncher.mods.elfeatures.texture.provider;

import com.mojang.authlib.GameProfile;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProperty;
import org.easylauncher.mods.elfeatures.texture.model.LegacyTexturesPayload;
import org.easylauncher.mods.elfeatures.texture.model.LegacyTexturesResponse;
import org.easylauncher.mods.elfeatures.util.ExpiringCache;
import org.easylauncher.mods.elfeatures.util.UsernameValidator;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class LegacyMojangTexturesProvider extends LegacyTexturesProviderBase<UUID> {

    private static final String MOJANG_PROFILE_URL_PATTERN = "https://api.mojang.com/users/profiles/minecraft/%s";

    // a name Mojang doesn't know is as worth remembering as one it does
    private final ExpiringCache<String, UUID> idsCache = new ExpiringCache<>(60L, TimeUnit.SECONDS, this::requestId);

    public LegacyMojangTexturesProvider(String userAgent) {
        super(userAgent);
    }

    /** 1.6-1.7.5 ask for a texture by name, while the session server only answers to a UUID. */
    public Map<LegacyProfileTexture.Type, LegacyProfileTexture> loadTexturesMapByName(String name) {
        if (!UsernameValidator.isValidUsername(name))
            return new HashMap<>();

        UUID id = idsCache.get(name);
        return id != null ? loadTexturesMap(id) : new HashMap<>();
    }

    @Override
    protected UUID keyFromProfile(GameProfile profile) {
        return profile != null ? idOfProfile(profile) : null;
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

    private UUID requestId(String name) {
        try {
            byte[] rawResponseBody = fetch(String.format(MOJANG_PROFILE_URL_PATTERN, name), name);
            if (rawResponseBody == null)
                return null;

            String jsonString = new String(rawResponseBody, StandardCharsets.UTF_8);
            return gson.fromJson(jsonString, LegacyTexturesResponse.class).getId();
        } catch (Exception cause) {
            logger.warn("Mojang profile of '{}' not loaded: {}", name, cause);
            return null;
        }
    }

}

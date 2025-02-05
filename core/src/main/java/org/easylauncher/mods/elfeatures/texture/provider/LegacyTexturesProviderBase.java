package org.easylauncher.mods.elfeatures.texture.provider;

import com.mojang.authlib.GameProfile;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture.Type;
import org.easylauncher.mods.elfeatures.texture.model.LegacyTexturesData;
import org.easylauncher.mods.elfeatures.texture.model.LegacyTexturesPayload;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

abstract class LegacyTexturesProviderBase<K> extends TexturesProviderBase<K, LegacyTexturesData, LegacyTexturesPayload> {

    LegacyTexturesProviderBase(String userAgent, LoggingFacade logger) {
        super(userAgent, logger);
    }

    @Override
    protected LegacyTexturesData emptyTexturesData() {
        return LegacyTexturesData.EMPTY;
    }

    @Override
    public LegacyTexturesPayload parseTexturesPayload(byte[] rawResponseBody) {
        String jsonString = new String(rawResponseBody, StandardCharsets.UTF_8);
        return gson.fromJson(jsonString, LegacyTexturesPayload.class);
    }

    @Override
    protected LegacyTexturesData parseTexturesData(K key, byte[] rawResponseBody) {
        Map<Type, LegacyProfileTexture> texturesMap = parseTexturesPayload(rawResponseBody).getTextures();
        logger.log("Received textures for '%s': '%s'", key, texturesMap);

        if (texturesMap != null && !texturesMap.isEmpty()) {
            String propertyValue = Base64.getEncoder().encodeToString(rawResponseBody);
            return new LegacyTexturesData(propertyValue, texturesMap);
        } else {
            return LegacyTexturesData.EMPTY;
        }
    }

    public Map<LegacyProfileTexture.Type, LegacyProfileTexture> loadTexturesMap(GameProfile profile) {
        return loadTexturesMap(keyFromProfile(profile));
    }

    public Map<LegacyProfileTexture.Type, LegacyProfileTexture> loadTexturesMap(K key) {
        if (!validateKey(key))
            return new HashMap<>();

        LegacyTexturesData loaded = texturesCache.getUnchecked(key);
        return loaded != null ? new HashMap<>(loaded.getTexturesMap()) : new HashMap<>();
    }

}

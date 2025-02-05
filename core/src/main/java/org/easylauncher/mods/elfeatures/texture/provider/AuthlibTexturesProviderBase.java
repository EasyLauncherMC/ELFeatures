package org.easylauncher.mods.elfeatures.texture.provider;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import org.easylauncher.mods.elfeatures.texture.model.AuthlibTexturesData;
import org.easylauncher.mods.elfeatures.texture.model.AuthlibTexturesPayload;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

abstract class AuthlibTexturesProviderBase<K> extends TexturesProviderBase<K, AuthlibTexturesData, AuthlibTexturesPayload> {

    AuthlibTexturesProviderBase(String userAgent, LoggingFacade logger) {
        super(userAgent, logger);
    }

    @Override
    protected AuthlibTexturesData emptyTexturesData() {
        return AuthlibTexturesData.EMPTY;
    }

    @Override
    public AuthlibTexturesPayload parseTexturesPayload(byte[] rawResponseBody) {
        String jsonString = new String(rawResponseBody, StandardCharsets.UTF_8);
        return gson.fromJson(jsonString, AuthlibTexturesPayload.class);
    }

    @Override
    protected AuthlibTexturesData parseTexturesData(K key, byte[] rawResponseBody) {
        Map<Type, MinecraftProfileTexture> texturesMap = parseTexturesPayload(rawResponseBody).getTextures();
        logger.log("Received textures for '%s': '%s'", key, texturesMap);

        if (texturesMap != null && !texturesMap.isEmpty()) {
            String propertyValue = Base64.getEncoder().encodeToString(rawResponseBody);
            return new AuthlibTexturesData(propertyValue, texturesMap);
        } else {
            return AuthlibTexturesData.EMPTY;
        }
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(GameProfile profile) {
        return loadTexturesMap(keyFromProfile(profile), null);
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(GameProfile profile, Map<Type, MinecraftProfileTexture> textures) {
        return loadTexturesMap(keyFromProfile(profile), textures);
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(K key) {
        return loadTexturesMap(key, null);
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(K key, Map<Type, MinecraftProfileTexture> textures) {
        if (textures == null || textures instanceof ImmutableMap)
            textures = new HashMap<>();

        if (!textures.isEmpty() || !validateKey(key))
            return textures;

        AuthlibTexturesData loaded = texturesCache.getUnchecked(key);
        if (loaded != null)
            textures.putAll(loaded.getTexturesMap());

        return textures;
    }

}

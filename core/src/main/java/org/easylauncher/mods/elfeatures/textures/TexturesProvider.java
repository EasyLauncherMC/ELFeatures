package org.easylauncher.mods.elfeatures.textures;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;
import org.easylauncher.mods.elfeatures.util.UsernameValidator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class TexturesProvider extends CacheLoader<String, TexturesData> {

    private static final String TEXTURES_URL_PATTERN = "http://textures.easyxcdn.net/users/%s.json";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final String userAgent;
    private final LoggingFacade logger;
    private final Gson gson;
    private final LoadingCache<String, TexturesData> texturesCache;

    public TexturesProvider(String userAgent, LoggingFacade logger) {
        this.userAgent = userAgent;
        this.logger = logger;
        this.gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
        this.texturesCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60L, TimeUnit.SECONDS)
                .build(this);
    }

    @Override
    public TexturesData load(String username) {
        // skip textures loading for invalid username
        if (!UsernameValidator.isValidUsername(username))
            return TexturesData.EMPTY;

        try {
            URL url = new URI(String.format(TEXTURES_URL_PATTERN, username.toLowerCase())).toURL();
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                httpConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                httpConnection.setReadTimeout(READ_TIMEOUT_MS);
                httpConnection.setUseCaches(false);
                httpConnection.setRequestProperty("User-Agent", userAgent);

                int responseCode = httpConnection.getResponseCode();
                if (responseCode != 200) {
                    logger.log("Textures for '%s' not found (response code: %d)", username, responseCode);
                    return TexturesData.EMPTY;
                }

                int contentLength = httpConnection.getContentLength();
                if (contentLength <= 0) {
                    logger.log("Textures for '%s' not found (invalid content length: %d)", username, contentLength);
                    return TexturesData.EMPTY;
                }

                try (InputStream inputStream = httpConnection.getInputStream()) {
                    byte[] rawResponseBody = new byte[contentLength];
                    int read = inputStream.read(rawResponseBody);
                    if (read != contentLength) {
                        logger.log("Textures for '%s' not found (content length/bytes read mismatch)", username);
                        return TexturesData.EMPTY;
                    }

                    TexturesPayload payload = parseTexturesPayload(rawResponseBody);
                    Map<Type, MinecraftProfileTexture> texturesMap = payload.getTextures();
                    logger.log("Received textures for '%s': '%s'", username, texturesMap);

                    if (texturesMap != null && !texturesMap.isEmpty()) {
                        String propertyValue = Base64.getEncoder().encodeToString(rawResponseBody);
                        return new TexturesData(propertyValue, texturesMap);
                    } else {
                        return TexturesData.EMPTY;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return TexturesData.EMPTY;
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(GameProfile profile) {
        return loadTexturesMap(profile.getName(), null);
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(GameProfile profile, Map<Type, MinecraftProfileTexture> textures) {
        return loadTexturesMap(profile.getName(), textures);
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(String username) {
        return loadTexturesMap(username, null);
    }

    public Map<Type, MinecraftProfileTexture> loadTexturesMap(String username, Map<Type, MinecraftProfileTexture> textures) {
        if (textures == null || textures instanceof ImmutableMap)
            textures = new HashMap<>();

        if (!textures.isEmpty() || !UsernameValidator.isValidUsername(username))
            return textures;

        TexturesData loaded = texturesCache.getUnchecked(username);
        if (loaded != null)
            textures.putAll(loaded.getTexturesMap());

        return textures;
    }

    public Property loadTexturesProperty(GameProfile profile) {
        return loadTexturesProperty(profile.getName());
    }

    public Property loadTexturesProperty(String username) {
        logger.log("Requesting textures property for '%s'%n", username);
        TexturesData loaded = texturesCache.getUnchecked(username);
        String propertyValue = loaded != null ? loaded.getPropertyValue() : null;
        return propertyValue != null ? new Property("textures", propertyValue) : null;
    }

    public TexturesPayload parseTexturesPayload(byte[] data) {
        String jsonString = new String(data, StandardCharsets.UTF_8);
        return gson.fromJson(jsonString, TexturesPayload.class);
    }

}

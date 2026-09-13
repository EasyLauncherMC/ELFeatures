package org.easylauncher.mods.elfeatures.texture.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import org.easylauncher.mods.elfeatures.texture.model.TexturesData;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;
import org.easylauncher.mods.elfeatures.util.UuidTypeAdapter;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

abstract class TexturesProviderBase<K, D extends TexturesData, P> extends CacheLoader<K, D> {

    protected static final String EASYX_TEXTURES_URL_PATTERN = "http://textures.easyxcdn.net/users/%s.json";
    protected static final String MOJANG_TEXTURES_URL_PATTERN = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    protected static final int CONNECT_TIMEOUT_MS = 3000;
    protected static final int READ_TIMEOUT_MS = 3000;
    private static final int MAX_ATTEMPTS = 3;

    protected final String userAgent;
    protected final LoggingFacade logger;
    protected final Gson gson;
    protected final LoadingCache<K, D> texturesCache;

    TexturesProviderBase(String userAgent, LoggingFacade logger) {
        this.userAgent = userAgent;
        this.logger = logger;
        this.gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UuidTypeAdapter()).create();
        this.texturesCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60L, TimeUnit.SECONDS)
                .build(this);
    }

    protected abstract K keyFromProfile(GameProfile profile);

    protected abstract String formatTexturesUrl(K key);

    protected abstract D emptyTexturesData();

    public abstract P parseTexturesPayload(byte[] rawResponseBody);

    protected abstract D parseTexturesData(K key, byte[] rawResponseBody);

    protected boolean validateKey(K key) {
        return key != null;
    }

    @Override
    public D load(K key) {
        if (!validateKey(key))
            return emptyTexturesData();

        // whatever comes back is cached, so a single dropped request would leave the player without a skin
        for (int attempt = 1; ; attempt++) {
            try {
                return request(key);
            } catch (IOException cause) {
                if (attempt == MAX_ATTEMPTS) {
                    logger.log("Textures for '%s' not loaded: %s", key, cause);
                    return emptyTexturesData();
                }

                logger.log("Textures for '%s' not loaded (attempt %d of %d), retrying: %s", key, attempt, MAX_ATTEMPTS, cause);
            } catch (Exception cause) {
                logger.log("Textures for '%s' not loaded: %s", key, cause);
                return emptyTexturesData();
            }
        }
    }

    private D request(K key) throws Exception {
        URL url = new URI(formatTexturesUrl(key)).toURL();
        URLConnection urlConnection = url.openConnection();
        if (!(urlConnection instanceof HttpURLConnection))
            return emptyTexturesData();

        HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
        httpConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        httpConnection.setReadTimeout(READ_TIMEOUT_MS);
        httpConnection.setUseCaches(false);
        httpConnection.setRequestProperty("User-Agent", userAgent);

        int responseCode = httpConnection.getResponseCode();
        if (responseCode >= 500)
            throw new IOException("Server error (response code: " + responseCode + ")");

        if (responseCode != 200) {
            logger.log("Textures for '%s' not found (response code: %d)", key, responseCode);
            return emptyTexturesData();
        }

        int contentLength = httpConnection.getContentLength();
        if (contentLength <= 0) {
            logger.log("Textures for '%s' not found (invalid content length: %d)", key, contentLength);
            return emptyTexturesData();
        }

        // a single read() hands back whatever has arrived so far, which can be less than the whole body
        try (DataInputStream inputStream = new DataInputStream(httpConnection.getInputStream())) {
            byte[] rawResponseBody = new byte[contentLength];
            inputStream.readFully(rawResponseBody);
            return parseTexturesData(key, rawResponseBody);
        }
    }

    public Property loadTexturesProperty(GameProfile profile) {
        return loadTexturesProperty(keyFromProfile(profile));
    }

    public Property loadTexturesProperty(K key) {
        logger.log("Requesting textures property for '%s'%n", key);
        D loaded = texturesCache.getUnchecked(key);
        String propertyValue = loaded != null ? loaded.getPropertyValue() : null;
        return propertyValue != null ? new Property("textures", propertyValue) : null;
    }

    // -------------- INTERNAL -----------------------------------------------------------------------------------------

    @SneakyThrows
    protected final UUID idOfProfile(GameProfile profile) {
        return GameProfileAccessors.ID != null
                ? (UUID) GameProfileAccessors.ID.invoke(profile)
                : profile.getId();
    }

    @SneakyThrows
    protected final String nameOfProfile(GameProfile profile) {
        return GameProfileAccessors.NAME != null
                ? (String) GameProfileAccessors.NAME.invoke(profile)
                : profile.getName();
    }

    /**
     * GameProfile is a record starting from authlib 7.x.
     *
     * <p>Looked up on first use rather than when a provider loads: 1.6 has no authlib, and a provider of it has to
     * load all the same.
     */
    private static final class GameProfileAccessors {

        private static final MethodHandle ID = findAccessorOrNull("id", UUID.class);
        private static final MethodHandle NAME = findAccessorOrNull("name", String.class);

        private static MethodHandle findAccessorOrNull(String name, Class<?> type) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup().in(GameProfile.class);
                return lookup.findVirtual(GameProfile.class, name, MethodType.methodType(type));
            } catch (NoSuchMethodException ignored) {
                return null;
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

}

package org.easylauncher.mods.elfeatures.texture.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.easylauncher.mods.elfeatures.texture.model.TexturesData;
import org.easylauncher.mods.elfeatures.util.LoggingFacade;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

abstract class TexturesProviderBase<K, D extends TexturesData, P> extends CacheLoader<K, D> {

    protected static final String EASYX_TEXTURES_URL_PATTERN = "http://textures.easyxcdn.net/users/%s.json";
    protected static final String MOJANG_TEXTURES_URL_PATTERN = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    protected static final int CONNECT_TIMEOUT_MS = 5000;
    protected static final int READ_TIMEOUT_MS = 5000;

    protected final String userAgent;
    protected final LoggingFacade logger;
    protected final Gson gson;
    protected final LoadingCache<K, D> texturesCache;

    TexturesProviderBase(String userAgent, LoggingFacade logger) {
        this.userAgent = userAgent;
        this.logger = logger;
        this.gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
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

        try {
            URL url = new URI(formatTexturesUrl(key)).toURL();
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                httpConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                httpConnection.setReadTimeout(READ_TIMEOUT_MS);
                httpConnection.setUseCaches(false);
                httpConnection.setRequestProperty("User-Agent", userAgent);

                int responseCode = httpConnection.getResponseCode();
                if (responseCode != 200) {
                    logger.log("Textures for '%s' not found (response code: %d)", key, responseCode);
                    return emptyTexturesData();
                }

                int contentLength = httpConnection.getContentLength();
                if (contentLength <= 0) {
                    logger.log("Textures for '%s' not found (invalid content length: %d)", key, contentLength);
                    return emptyTexturesData();
                }

                try (InputStream inputStream = httpConnection.getInputStream()) {
                    byte[] rawResponseBody = new byte[contentLength];
                    int read = inputStream.read(rawResponseBody);
                    if (read != contentLength) {
                        logger.log("Textures for '%s' not found (content length/bytes read mismatch)", key);
                        return emptyTexturesData();
                    }

                    return parseTexturesData(key, rawResponseBody);
                }
            }
        } catch (Exception ignored) {
        }

        return emptyTexturesData();
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

}

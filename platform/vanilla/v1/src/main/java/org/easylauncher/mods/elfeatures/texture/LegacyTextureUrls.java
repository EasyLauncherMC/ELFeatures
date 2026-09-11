package org.easylauncher.mods.elfeatures.texture;

import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture;
import org.easylauncher.mods.elfeatures.texture.provider.LegacyEasyxTexturesProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Where a skin or a cape of 1.6-1.7.5 is downloaded from: the game asks Mojang's legacy URL by the player's name,
 * and a player EasyX has textures for is sent to those instead.
 *
 * <p>Asked on the thread the image is downloaded on, so looking the name up with EasyX holds nothing up.
 */
public final class LegacyTextureUrls {

    private static final Pattern MOJANG_URL = Pattern.compile("^https?://skins\\.minecraft\\.net/Minecraft(Skins|Cloaks)/(.+)\\.png$");

    private LegacyTextureUrls() {}

    public static String resolve(String url) {
        Matcher matcher = MOJANG_URL.matcher(url);
        if (!matcher.matches())
            return url;

        LegacyProfileTexture.Type type = "Skins".equals(matcher.group(1))
                ? LegacyProfileTexture.Type.SKIN
                : LegacyProfileTexture.Type.CAPE;

        LegacyEasyxTexturesProvider texturesProvider = ELFeaturesMod.legacyEasyxTexturesProvider();
        LegacyProfileTexture texture = texturesProvider.loadTexturesMap(matcher.group(2)).get(type);
        return texture != null && texture.getUrl() != null ? texture.getUrl() : url;
    }

}

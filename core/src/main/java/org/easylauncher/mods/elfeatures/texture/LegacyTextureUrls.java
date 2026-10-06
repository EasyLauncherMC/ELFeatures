package org.easylauncher.mods.elfeatures.texture;

import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Where a skin or a cape up to 1.7.5 is downloaded from: the game asks Mojang's legacy URL by the player's name,
 * which is dead. A player EasyX has textures for is sent to those, anyone else to their Mojang ones.
 *
 * <p>Asked on the thread the image is downloaded on, so the lookups hold nothing up.
 */
public final class LegacyTextureUrls {

    // up to 1.2.5 the textures lay right in the S3 buckets
    private static final Pattern MOJANG_URL = Pattern.compile(
            "^https?://(?:skins\\.minecraft\\.net|s3\\.amazonaws\\.com)/Minecraft(Skins|Cloaks)/(.+)\\.png$"
    );

    // a texture nobody has: the game passes a 404 over quietly,
    // while the dead host drops the connection and gets logged
    private static final String MISSING_URL = "http://skins.easyxcdn.net/none";

    private LegacyTextureUrls() {}

    public static String resolve(String url) {
        Matcher matcher = MOJANG_URL.matcher(url);
        if (!matcher.matches())
            return url;

        String name = matcher.group(2);
        LegacyProfileTexture.Type type = "Skins".equals(matcher.group(1))
                ? LegacyProfileTexture.Type.SKIN
                : LegacyProfileTexture.Type.CAPE;

        // Mojang only for a name EasyX knows nothing about: an EasyX player without a cape gets no stranger's one
        Map<LegacyProfileTexture.Type, LegacyProfileTexture> textures = ELFeaturesMod.legacyEasyxTexturesProvider().loadTexturesMap(name);
        if (textures.isEmpty()) textures = ELFeaturesMod.legacyMojangTexturesProvider().loadTexturesMapByName(name);

        LegacyProfileTexture texture = textures.get(type);
        return texture != null && texture.getUrl() != null ? texture.getUrl() : MISSING_URL;
    }

}

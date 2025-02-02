package org.easylauncher.mods.elfeatures.textures;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public final class TexturesData {

    public static final TexturesData EMPTY = new TexturesData(null, Collections.emptyMap());

    private final String propertyValue;
    private final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> texturesMap;

}

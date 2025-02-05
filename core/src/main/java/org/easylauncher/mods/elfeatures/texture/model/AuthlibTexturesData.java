package org.easylauncher.mods.elfeatures.texture.model;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public final class AuthlibTexturesData implements TexturesData {

    public static final AuthlibTexturesData EMPTY = new AuthlibTexturesData(null, Collections.emptyMap());

    private final String propertyValue;
    private final Map<Type, MinecraftProfileTexture> texturesMap;

}

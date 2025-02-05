package org.easylauncher.mods.elfeatures.texture.model;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public final class AuthlibTexturesPayload {

    private UUID profileId;
    private String profileName;
    private Map<Type, MinecraftProfileTexture> textures;

}

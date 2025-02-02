package org.easylauncher.mods.elfeatures.textures;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.Getter;

import java.util.Map;

@Getter
public final class TexturesPayload {

    private String profileName;
    private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;

}

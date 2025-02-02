package com.mojang.authlib.minecraft;

import com.mojang.authlib.SignatureState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter @Accessors(fluent = true)
@AllArgsConstructor
public class MinecraftProfileTextures {

    public static final MinecraftProfileTextures EMPTY = new MinecraftProfileTextures(null, null, null, SignatureState.SIGNED);

    private final MinecraftProfileTexture skin;
    private final MinecraftProfileTexture cape;
    private final MinecraftProfileTexture elytra;
    private final SignatureState signatureState;

}
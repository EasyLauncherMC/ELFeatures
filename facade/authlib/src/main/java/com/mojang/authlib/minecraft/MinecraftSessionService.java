package com.mojang.authlib.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.util.Map;

public interface MinecraftSessionService {

    Property getPackedTextures(GameProfile profile);

    MinecraftProfileTextures unpackTextures(Property property);

    Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure);

    default MinecraftProfileTextures getTextures(GameProfile profile) {
        Property packed = this.getPackedTextures(profile);
        return packed != null ? this.unpackTextures(packed) : MinecraftProfileTextures.EMPTY;
    }

}

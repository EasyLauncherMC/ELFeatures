package net.minecraft.client.resources;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;

import java.util.Map;
import java.util.UUID;

public class SkinManager {

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile profile) {
        return null;
    }

    public static class TextureInfo { }

    public record CacheKey(UUID profileId, Property packedTextures) {}

}

package org.easylauncher.mods.elfeatures.mixin.textures;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.textures.TexturesInspector;
import org.easylauncher.mods.elfeatures.textures.TexturesPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Base64;
import java.util.Map;

public final class MixinSkinManager1 {

    @Mixin(targets = "net/minecraft/client/resources/SkinManager$1")
    public static abstract class V1 {

        @Redirect(
                method = "Lnet/minecraft/client/resources/SkinManager$1;lambda$load$0(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/resources/SkinManager$TextureInfo;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;",
                        ordinal = 0
                )
        )
        private static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_load(
                MinecraftSessionService sessionService,
                GameProfile profile,
                boolean requireSecure
        ) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> texturesMap = Maps.newHashMap();

            try {
                texturesMap.putAll(sessionService.getTextures(profile, requireSecure));
            } catch (Exception ignored) {
            }

            if (texturesMap.isEmpty())
                ELFeaturesMod.texturesProvider().loadTexturesMap(profile, texturesMap);

            return texturesMap;
        }

    }

    @Mixin(targets = "net/minecraft/client/resources/SkinManager$1")
    public static abstract class V2 {

        @Redirect(
                method = "lambda$load$0(Lnet/minecraft/client/resources/SkinManager$CacheKey;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;unpackTextures(Lcom/mojang/authlib/properties/Property;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;"
                )
        )
        private static MinecraftProfileTextures redirect_unpackTextures(
                MinecraftSessionService sessionService,
                Property property
        ) {
            try {
                byte[] decodedData = Base64.getDecoder().decode(property.value());
                TexturesPayload payload = ELFeaturesMod.texturesProvider().parseTexturesPayload(decodedData);
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = payload.getTextures();

                boolean foundEasyX = textures.values().stream()
                        .map(MinecraftProfileTexture::getUrl)
                        .anyMatch(TexturesInspector::hasEasyxDomain);

                if (foundEasyX) {
                    return new MinecraftProfileTextures(
                            textures.get(MinecraftProfileTexture.Type.SKIN),
                            textures.get(MinecraftProfileTexture.Type.CAPE),
                            textures.get(MinecraftProfileTexture.Type.ELYTRA),
                            SignatureState.SIGNED
                    );
                }
            } catch (Throwable ignored) {
            }

            return sessionService.unpackTextures(property);
        }

    }

}

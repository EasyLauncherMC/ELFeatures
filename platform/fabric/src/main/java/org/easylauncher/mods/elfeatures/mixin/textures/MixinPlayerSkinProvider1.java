package org.easylauncher.mods.elfeatures.mixin.textures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.texture.TexturesInspector;
import org.easylauncher.mods.elfeatures.texture.model.AuthlibTexturesPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Base64;
import java.util.Map;

public final class MixinPlayerSkinProvider1 {

    @Mixin(targets = "net/minecraft/client/texture/PlayerSkinProvider$1")
    public static abstract class V1 {

        @Redirect(
                method = "load(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;"
                )
        )
        private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_load(
                MinecraftSessionService sessionService,
                GameProfile profile,
                boolean requireSecure
        ) {
            return ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesMap(profile, sessionService.getTextures(profile, requireSecure));
        }

    }

    @Mixin(targets = "net/minecraft/client/texture/PlayerSkinProvider$1")
    public static abstract class V2 {

        @Redirect(
                method = "method_52867(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/texture/PlayerSkinProvider$Textures;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;"
                )
        )
        private static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_m52867(
                MinecraftSessionService sessionService,
                GameProfile profile,
                boolean requireSecure
        ) {
            return ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesMap(profile, sessionService.getTextures(profile, requireSecure));
        }

    }

    @Mixin(targets = "net/minecraft/client/texture/PlayerSkinProvider$1")
    public static abstract class V3 {

        @Redirect(
                method = "method_54647(Lnet/minecraft/client/texture/PlayerSkinProvider$Key;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;",
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
                AuthlibTexturesPayload payload = ELFeaturesMod.authlibEasyxTexturesProvider().parseTexturesPayload(decodedData);
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

package org.easylauncher.mods.elfeatures.mixin.textures;

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

@Mixin(targets = "net/minecraft/client/resources/SkinManager$1")
public final class MixinSkinManager1 {

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

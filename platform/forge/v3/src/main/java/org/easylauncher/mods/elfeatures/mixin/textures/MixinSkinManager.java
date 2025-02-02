package org.easylauncher.mods.elfeatures.mixin.textures;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.resources.SkinManager;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

public final class MixinSkinManager {

    @Mixin(SkinManager.class)
    public static abstract class V1 {

        @Inject(
                method = "Lnet/minecraft/client/resources/SkinManager;getInsecureSkinInformation(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;",
                at = @At(value = "RETURN"),
                cancellable = true
        )
        private void inject_getInsecureSkinInformation(
                GameProfile profile,
                CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> callbackInfo
        ) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> texturesMap = callbackInfo.getReturnValue();
            callbackInfo.setReturnValue(ELFeaturesMod.texturesProvider().loadTexturesMap(profile, texturesMap));
        }

        @Redirect(
                method = "Lnet/minecraft/client/resources/SkinManager;lambda$registerSkins$4(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)V",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;",
                        ordinal = 0
                )
        )
        private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_registerSkins(
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

    @Mixin(SkinManager.class)
    public static abstract class V2 {

        @Redirect(
                method = "Lnet/minecraft/client/resources/SkinManager;getOrLoad(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getPackedTextures(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/properties/Property;"
                )
        )
        private Property redirect_getPackedTextures(MinecraftSessionService sessionService, GameProfile profile) {
            Property packedTextures = sessionService.getPackedTextures(profile);

            if (packedTextures == null)
                packedTextures = ELFeaturesMod.texturesProvider().loadTexturesProperty(profile);

            return packedTextures;
        }

    }

}

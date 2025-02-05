package org.easylauncher.mods.elfeatures.mixin.textures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

public final class MixinPlayerSkinProvider {

    @Mixin(PlayerSkinProvider.class)
    public static abstract class V1 {

        @Redirect(
                method = "method_4653(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/texture/PlayerSkinProvider$SkinTextureAvailableCallback;)V",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;"
                )
        )
        private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_m4653(
                MinecraftSessionService sessionService,
                GameProfile profile,
                boolean requireSecure
        ) {
            return ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesMap(profile, sessionService.getTextures(profile, requireSecure));
        }

    }

    @Mixin(PlayerSkinProvider.class)
    public static abstract class V2 {

        @Inject(
                method = "getTextures(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;",
                at = @At("RETURN"),
                cancellable = true
        )
        private void inject_getTextures(
                GameProfile profile,
                CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> callbackInfo
        ) {
            callbackInfo.setReturnValue(ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesMap(profile, callbackInfo.getReturnValue()));
        }

        @Redirect(
                method = "method_4653(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/texture/PlayerSkinProvider$SkinTextureAvailableCallback;)V",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;"
                )
        )
        private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_m4653(
                MinecraftSessionService sessionService,
                GameProfile profile,
                boolean requireSecure
        ) {
            return ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesMap(profile, sessionService.getTextures(profile, requireSecure));
        }

    }

    @Mixin(PlayerSkinProvider.class)
    public static abstract class V3 {

        @Redirect(
                method = "fetchSkinTextures(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getPackedTextures(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/properties/Property;"
                )
        )
        private Property redirect_getPackedTextures(MinecraftSessionService sessionService, GameProfile profile) {
            Property packedTextures = sessionService.getPackedTextures(profile);

            if (packedTextures == null)
                packedTextures = ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesProperty(profile);

            return packedTextures;
        }

    }

}

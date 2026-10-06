package org.easylauncher.mods.elfeatures.mixin.textures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.SessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.resources.SkinManager;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public final class MixinSkinProvider {

    @Mixin(SkinManager.class)
    public static abstract class V1 {

        @Redirect(
                method = "get(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getPackedTextures(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/properties/Property;"
                )
        )
        private Property redirect_getPackedTextures(MinecraftSessionService sessionService, GameProfile profile) {
            Property packedTextures = sessionService.getPackedTextures(profile);
            return packedTextures != null ? packedTextures : easyxTextures(profile);
        }

    }

    @Mixin(SkinManager.class)
    public static abstract class V2 {

        @Redirect(
                method = "get(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
                at = @At(
                        value = "INVOKE",
                        target = "Lcom/mojang/authlib/minecraft/SessionService;getPackedTextures(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/properties/Property;"
                )
        )
        private Property redirect_getPackedTextures(SessionService sessionService, GameProfile profile) {
            Property packedTextures = sessionService.getPackedTextures(profile);
            return packedTextures != null ? packedTextures : easyxTextures(profile);
        }

    }

    public static Property easyxTextures(GameProfile profile) {
        return ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesProperty(profile);
    }

}

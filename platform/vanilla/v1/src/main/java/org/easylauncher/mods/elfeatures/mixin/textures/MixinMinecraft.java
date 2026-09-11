package org.easylauncher.mods.elfeatures.mixin.textures;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import org.easylauncher.mods.elfeatures.texture.EasyxSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Redirect(
            method = "<init>",
            remap = false,
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;"
            )
    )
    private MinecraftSessionService redirect_createMinecraftSessionService(YggdrasilAuthenticationService authService) {
        return EasyxSessionService.wrap(authService.createMinecraftSessionService());
    }

}

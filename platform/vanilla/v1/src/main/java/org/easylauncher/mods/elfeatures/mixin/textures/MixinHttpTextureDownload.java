package org.easylauncher.mods.elfeatures.mixin.textures;

import org.easylauncher.mods.elfeatures.texture.LegacyTextureUrls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = {
        "net.minecraft.client.render.texture.HttpTexture__34366662",
        "net.minecraft.client.render.texture.HttpTexture$34366662",
})
public abstract class MixinHttpTextureDownload {

    @ModifyArg(
            method = "run",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/net/URL;<init>(Ljava/lang/String;)V"
            )
    )
    private String modifyArg_url(String url) {
        return LegacyTextureUrls.resolve(url);
    }

}

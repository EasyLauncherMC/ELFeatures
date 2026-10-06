package org.easylauncher.mods.elfeatures.mixin.textures;

import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * 1.7.7 and 1.7.8 load textures only for a version 4 UUID, and neither an EasyX account nor an offline player has one.
 */
@Pseudo
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {

    @Redirect(
            method = "<init>",
            remap = false,
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/UUID;version()I"
            )
    )
    private int redirect_version(UUID uuid) {
        return 4;
    }

}

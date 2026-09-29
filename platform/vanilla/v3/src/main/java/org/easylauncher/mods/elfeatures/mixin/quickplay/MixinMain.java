package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.client.main.Main;
import org.easylauncher.mods.elfeatures.activity.QuickPlayWorldHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Main.class)
public abstract class MixinMain {

    @ModifyVariable(
            method = "main([Ljava/lang/String;)V",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private static String[] modifyVariable_main(String[] args) {
        return QuickPlayWorldHolder.fixWorldArgument(args);
    }

}

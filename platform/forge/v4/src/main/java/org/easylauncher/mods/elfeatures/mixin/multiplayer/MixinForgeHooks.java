package org.easylauncher.mods.elfeatures.mixin.multiplayer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

/**
 * Native {@code --quickPlaySingleplayer} starts ticking the integrated server before Forge has run
 * {@code AddReloadListenerEvent}, so the first leaf decay (and anything else that rolls loot) throws
 * {@code Can not retrieve LootModifierManager until resources have loaded once}. Until that listener
 * has fired there are no modifiers to apply, so the vanilla drops stand.
 */
@Mixin(value = ForgeHooks.class, remap = false)
public abstract class MixinForgeHooks {

    @Inject(
            method = "modifyLoot",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void elfeatures$skipUntilLoaded(
            ResourceLocation name,
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context,
            CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir
    ) {
        if (elfeatures$lootModifiersUnready()) {
            cir.setReturnValue(generatedLoot);
        }
    }

    private static boolean elfeatures$lootModifiersUnready() {
        try {
            @SuppressWarnings({"Java9ReflectionClassVisibility", "JavaReflectionMemberAccess"})
            Field instance = Class.forName("net.minecraftforge.common.ForgeInternalHandler").getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            return instance.get(null) == null;
        } catch (Throwable ignored) {
            return false;
        }
    }

}

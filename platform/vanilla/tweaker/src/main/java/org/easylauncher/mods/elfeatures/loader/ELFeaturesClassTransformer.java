package org.easylauncher.mods.elfeatures.loader;

import net.minecraft.launchwrapper.IClassTransformer;
import org.easylauncher.mods.elfeatures.loader.mixin.MixinService;
import org.spongepowered.asm.mixin.MixinEnvironment;

public final class ELFeaturesClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return bytes;

        if (!isMinecraftClass(name))
            return bytes;

        String intermediaryName = ELFeaturesMixinBootstrap.getMixinRemapper().unmap(name);
        if (intermediaryName == null || intermediaryName.equals(name))
            return bytes;

        return MixinService.getTransformer().transformClass(MixinEnvironment.getCurrentEnvironment(), name, bytes);
    }

    public static boolean isMinecraftClass(String name) {
        if (name == null)
            return false;

        name = name.replace('/', '.');
        return !name.contains(".") || name.startsWith("net.minecraft.");
    }

}

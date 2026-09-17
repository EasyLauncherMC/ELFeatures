package org.easylauncher.mods.elfeatures;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixins that name authlib types are left off the versions that have none: 1.6 through 1.7.5. Mixin still merges a
 * missed {@code @Redirect} into the target, and reflecting over {@code Minecraft} then fails while looking up the
 * tick and the join.
 */
public final class ELFeaturesMixinPlugin implements IMixinConfigPlugin {

    private static final boolean HAS_AUTHLIB = classPresent(
            "com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService"
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return HAS_AUTHLIB || !mixinClassName.endsWith("textures.MixinMinecraft");
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name, false, ELFeaturesMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            return false;
        }
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}

package org.easylauncher.mods.elfeatures.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.mappingio.tree.MappingTree;
import org.easylauncher.mods.elfeatures.loader.mixin.MixinRemapper;
import org.easylauncher.mods.elfeatures.loader.mapping.MappingProvider;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Method;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ELFeaturesMixinBootstrap {

    private static boolean initialized = false;
    private static MixinRemapper mixinRemapper;

    public static void init() {
        if (initialized)
            throw new RuntimeException("ELFeaturesMixinBootstrap has already been initialized!");

        System.setProperty("mixin.bootstrapService", "org.easylauncher.mods.elfeatures.loader.mixin.MixinServiceBootstrap");
        System.setProperty("mixin.service", "org.easylauncher.mods.elfeatures.loader.mixin.MixinService");

        MixinBootstrap.init();

        MappingTree mappings = MappingProvider.loadMappings();
        mixinRemapper = new MixinRemapper(mappings, "intermediary", "official");
        MixinEnvironment.getDefaultEnvironment().getRemappers().add(mixinRemapper);

        Mixins.addConfiguration("elfeatures.mixins.json");

        try {
            Method method = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            method.setAccessible(true);
            method.invoke(null, MixinEnvironment.Phase.INIT);
            method.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        initialized = true;
    }

    public static MixinRemapper getMixinRemapper() {
        assert mixinRemapper != null;
        return mixinRemapper;
    }

}

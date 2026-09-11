package org.easylauncher.mods.elfeatures.loader;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.mappingio.tree.MappingTree;
import org.easylauncher.mods.elfeatures.loader.mapping.MappingProvider;
import org.easylauncher.mods.elfeatures.loader.mixin.MixinRemapper;
import org.easylauncher.mods.elfeatures.loader.mixin.MixinService;
import org.easylauncher.mods.elfeatures.loader.mixin.MixinServiceBootstrap;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Method;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ELFeaturesMixinBootstrap {

    private static final String MIXIN_CONFIG = "elfeatures.mixins.json";
    private static final String SOURCE_NAMESPACE = "intermediary";
    private static final String TARGET_NAMESPACE = "official";

    private static boolean initialized = false;
    private static MixinRemapper mixinRemapper;

    /**
     * Starts mixin and walks it up to the phase where configurations are actually applied.
     *
     * <p>The phase is stepped by reflection because nothing but a platform agent is supposed to do it, and this
     * loader is not one — mixin is running without any platform at all.
     */
    public static void init() {
        if (initialized)
            throw new IllegalStateException("ELFeaturesMixinBootstrap has already been initialized!");

        System.setProperty("mixin.bootstrapService", MixinServiceBootstrap.class.getName());
        System.setProperty("mixin.service", MixinService.class.getName());

        MixinBootstrap.init();

        // no mod loader here to do it, and the mixins use MixinExtras' @Local and @Share
        MixinExtrasBootstrap.init();

        // no remapper at all from 26.1 on
        MappingTree mappings = MappingProvider.loadMappings();
        if (mappings.getNamespaceId(SOURCE_NAMESPACE) != MappingTree.NULL_NAMESPACE_ID) {
            mixinRemapper = new MixinRemapper(mappings, SOURCE_NAMESPACE, TARGET_NAMESPACE);
            MixinEnvironment.getDefaultEnvironment().getRemappers().add(mixinRemapper);
        }

        Mixins.addConfiguration(MIXIN_CONFIG);

        try {
            Method method = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            method.setAccessible(true);
            method.invoke(null, MixinEnvironment.Phase.INIT);
            method.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (ReflectiveOperationException cause) {
            throw new IllegalStateException("Couldn't walk mixin up to its DEFAULT phase!", cause);
        }

        initialized = true;
    }

    public static MixinRemapper getMixinRemapper() {
        return mixinRemapper;
    }

}

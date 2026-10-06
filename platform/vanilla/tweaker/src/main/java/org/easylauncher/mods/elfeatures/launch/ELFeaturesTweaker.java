package org.easylauncher.mods.elfeatures.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

/**
 * The mod's way into a game a launch wrapper starts: any game OptiFine was installed into, and the vanilla one up
 * to 1.5.2.
 *
 * <p>Alone in its package on purpose: a launch wrapper excludes the package of every tweak class it loads from
 * its own loader, and everything else of ours has to stay inside that loader — the game's authlib lives there,
 * and a copy of ours linked against the system loader's authlib would not fit where the game passes it.
 *
 * <p>Nothing is set up here for the same reason. Registering the transformer by name is what gets the mod
 * loaded in the right place; bringing it up is the transformer's own constructor.
 */
public final class ELFeaturesTweaker implements ITweaker {

    private static final String LOADER_PACKAGE = "org.easylauncher.mods.elfeatures.loader.";
    private static final String TRANSFORMER = LOADER_PACKAGE + "ELFeaturesLaunchTransformer";

    @Override
    public void acceptOptions(List<String> arguments, File gameDirectory, File assetsDirectory, String profile) {
        // nothing to do here
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addTransformerExclusion(LOADER_PACKAGE);
        classLoader.registerTransformer(TRANSFORMER);
    }

    @Override
    public String getLaunchTarget() {
        // this tweaker never being the primary one
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

}

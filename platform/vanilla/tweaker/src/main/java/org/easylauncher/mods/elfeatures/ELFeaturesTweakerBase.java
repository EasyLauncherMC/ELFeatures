package org.easylauncher.mods.elfeatures;

import lombok.Getter;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
@Getter
public abstract class ELFeaturesTweakerBase extends ELFeaturesModBase implements ITweaker {

    public static final String LAUNCH_TARGET = System.getProperty("elfeatures.tweaker.launchTarget", "net.minecraft.client.main.Main");
    public static final boolean RUNNING_OPTIFINE = "true".equalsIgnoreCase(System.getProperty("elfeatures.running.optifine"));
    public static final String RUNNING_VERSION = Objects.requireNonNull(System.getProperty("elfeatures.running.version"));
    public static final boolean IS_PRIMARY_TWEAKER = ((List<ITweaker>) Launch.blackboard.get("Tweaks")).isEmpty();

    private static ELFeaturesTweakerBase INSTANCE;

    private LaunchClassLoader launchClassLoader;
    private List<String> args;

    public ELFeaturesTweakerBase(String modVersion, Logger logger) {
        super(String.format("ELFeatures/%s (%s)", modVersion, (RUNNING_OPTIFINE ? "OptiFine" : "Vanilla")), logger);
        INSTANCE = this;
    }

    public abstract String getModName();

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        log("Called #acceptOptions");

        log("args: %s", args);
        this.args = new ArrayList<>(args);

        log("gameDir: %s", gameDir);
        if (!args.contains("--gameDir")) {
            this.args.add("--gameDir");
            this.args.add(gameDir.getAbsolutePath());
        }

        log("assetsDir: %s", assetsDir);
        if (!args.contains("--assetsDir")) {
            this.args.add("--assetsDir");
            this.args.add(assetsDir.getAbsolutePath());
        }

        log("profile: %s", profile);
        if (!args.contains("--version")) {
            this.args.add("--version");
            this.args.add(profile);
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        this.launchClassLoader = launchClassLoader;
        launchClassLoader.addClassLoaderExclusion("org.objectweb.asm.");
        launchClassLoader.addClassLoaderExclusion("org.spongepowered.");
        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.loader.");
        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.util.");

        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.ELFeaturesMod");
        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.ELFeaturesModBase");
        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.ELFeaturesService");
        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.ELFeaturesTweaker");
        launchClassLoader.addClassLoaderExclusion("org.easylauncher.mods.elfeatures.ELFeaturesTweakerBase");

        launchClassLoader.registerTransformer("org.easylauncher.mods.elfeatures.loader.ELFeaturesClassTransformer");

        ELFeaturesMixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String getLaunchTarget() {
        return LAUNCH_TARGET;
    }

    @Override
    public String[] getLaunchArguments() {
        return IS_PRIMARY_TWEAKER ? args.toArray(new String[0]) : new String[0];
    }

    @Override
    public void log(String message, Object... args) {
        super.log("[ELFeaturesTweaker] %s", String.format(message, args));
    }

    public static ELFeaturesTweakerBase singleton() {
        return INSTANCE;
    }

}

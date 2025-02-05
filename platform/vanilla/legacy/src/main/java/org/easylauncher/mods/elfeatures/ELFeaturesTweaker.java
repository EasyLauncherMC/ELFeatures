package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public final class ELFeaturesTweaker extends ELFeaturesModBase implements ITweaker {

    public static final String DEFAULT_LAUNCH_TARGET = "net.minecraft.client.main.Main";
    public static final boolean RUNNING_OPTIFINE = "true".equalsIgnoreCase(System.getProperty("elfeatures.running.optifine"));

    private List<String> args;

    public ELFeaturesTweaker() {
        super(String.format("ELFeatures/%s (Vanilla)", Constants.MOD_VERSION), log);
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String version) {
        log("Called #acceptOptions");

        log("args: %s", args);
        this.args = new ArrayList<>(args);

        log("gameDir: %s", gameDir);
        this.args.add("--gameDir");
        this.args.add(gameDir.getAbsolutePath());

        log("assetsDir: %s", assetsDir);
        this.args.add("--assetsDir");
        this.args.add(assetsDir.getAbsolutePath());

        log("version: %s", version);
        this.args.add("--version");
        this.args.add(version);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        log("Called #injectIntoClassLoader");
        String transformerClassName = String.format("%s.ELFeaturesClassTransformer", getClass().getPackage().getName());
        log("transformerClassName = '%s'", transformerClassName);
        classLoader.registerTransformer(transformerClassName);
    }

    @Override
    public String getLaunchTarget() {
        log("Called #getLaunchTarget");
        return DEFAULT_LAUNCH_TARGET;
    }

    @Override
    public String[] getLaunchArguments() {
        log("Called #getLaunchArguments");
        return args.toArray(new String[0]);
    }

    @Override
    public void log(String message, Object... args) {
        super.log("[ELFeaturesTweaker] %s", String.format(message, args));
    }

}

package org.easylauncher.mods.elfeatures;

import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;
import cpw.mods.fml.relauncher.RelaunchLibraryManager;
import lombok.CustomLog;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Map;

/**
 * FML of 1.5-1.7.2 reads classes with ASM 4.1, which knows no Java 8 bytecode, so it is never handed ours: its
 * transformers skip them, the game is patched past its deobfuscation (1000), which would read the classes our calls
 * lead to, and the mod starts here rather than as an {@code @Mod} its discovery would have to read.
 *
 * <p>No MCVersion: FML leaves a core mod out on any version but the one it names, and this one serves 1.5.2-1.7.10.
 * The range is checked here instead, for the jar put into {@code mods/} by hand.
 */
@CustomLog
@IFMLLoadingPlugin.Name(Constants.MOD_NAME)
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("org.easylauncher.mods.elfeatures.")
public final class ELFeaturesFMLPlugin implements IFMLLoadingPlugin {

    /** FML of 1.5 relaunches the game in a class loader of its own, the one launchwrapper took over in 1.6. */
    private static final boolean RELAUNCHED = ELFeaturesFMLPlugin.class.getClassLoader().getClass().getName()
            .equals("cpw.mods.fml.relauncher.RelaunchClassLoader");

    // mccversion, set before any core mod is loaded
    private static final String MINECRAFT_VERSION = (String) FMLInjectionData.data()[4];
    private static final boolean SUPPORTED = MINECRAFT_VERSION.equals("1.5.2")
            || MINECRAFT_VERSION.startsWith("1.6.")
            || MINECRAFT_VERSION.startsWith("1.7.");

    /** The jar the mod came in, which {@link ELFeaturesModContainer} names as its source. */
    static File jar;

    public ELFeaturesFMLPlugin() throws URISyntaxException {
        // what FML does to a core mod jar found in mods/ that carries no FML mod; FML of 1.5 skips its libraries alike
        CodeSource codeSource = ELFeaturesFMLPlugin.class.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            jar = new File(codeSource.getLocation().toURI());
            (RELAUNCHED ? RelaunchLibraryManager.getLibraries() : CoreModManager.getLoadedCoremods()).add(jar.getName());
        }

        if (!SUPPORTED) {
            log.warn("This build serves Minecraft 1.5.2-1.7.10, not {}: left off", MINECRAFT_VERSION);
            return;
        }

        new ELFeaturesModForgeV1();
    }

    // FML of 1.5-1.6 has it in the interface
    @Override
    public String[] getLibraryRequestClass() {
        return null;
    }

    // FML of 1.5 knows no sorting index and would run it ahead of the deobfuscation: see injectData
    @Override
    public String[] getASMTransformerClass() {
        return RELAUNCHED || !SUPPORTED ? null : new String[] {MOD_TRANSFORMER_FQN};
    }

    // in place of the @Mod the discovery skipping this jar would have found
    @Override
    public String getModContainerClass() {
        return jar != null && SUPPORTED ? MOD_CONTAINER_FQN : null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    // FML of 1.5 registers its deobfuscation right before it calls this
    @Override
    public void injectData(Map<String, Object> map) {
        if (RELAUNCHED && SUPPORTED) {
            RelaunchClassLoader classLoader = (RelaunchClassLoader) ELFeaturesFMLPlugin.class.getClassLoader();
            classLoader.registerTransformer(RELAUNCH_TRANSFORMER_FQN);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    private static final String MOD_CONTAINER_FQN = "org.easylauncher.mods.elfeatures.ELFeaturesModContainer";
    private static final String MOD_TRANSFORMER_FQN = "org.easylauncher.mods.elfeatures.ELFeaturesTransformer";
    private static final String RELAUNCH_TRANSFORMER_FQN = "org.easylauncher.mods.elfeatures.ELFeaturesRelaunchTransformer";

}

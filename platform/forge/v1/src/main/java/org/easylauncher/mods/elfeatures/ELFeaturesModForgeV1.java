package org.easylauncher.mods.elfeatures;

import cpw.mods.fml.common.Mod;

@Mod(
        modid = "elfeatures",
        name = Constants.MOD_NAME,
        version = Constants.MOD_VERSION,
        useMetadata = true,
        acceptedMinecraftVersions = "1.7.10",
        acceptableRemoteVersions = "*" // client-side mod: never require it from the other side
)
public final class ELFeaturesModForgeV1 extends ELFeaturesModBase {

    public ELFeaturesModForgeV1() {
        super(String.format("ELFeatures/%s (Forge V1)", Constants.MOD_VERSION));
    }

}

package org.easylauncher.mods.elfeatures;

import net.minecraftforge.fml.common.Mod;

@Mod(
        value = "elfeatures",
        modid = "elfeatures",
        name = Constants.MOD_NAME,
        version = Constants.MOD_VERSION,
        useMetadata = true,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8,1.13)",
        acceptableRemoteVersions = "*" // client-side mod: never require it from the other side
)
public final class ELFeaturesModForgeV2 extends ELFeaturesModBase {

    public ELFeaturesModForgeV2() {
        super(String.format("ELFeatures/%s (Forge V2)", Constants.MOD_VERSION));
    }

}

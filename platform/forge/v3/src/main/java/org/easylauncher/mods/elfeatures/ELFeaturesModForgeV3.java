package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;

@Log4j2
@Mod(value = "elfeatures")
public final class ELFeaturesModForgeV3 extends ELFeaturesModBase {

    public ELFeaturesModForgeV3() {
        super(String.format("ELFeatures/%s (Forge V3)", Constants.MOD_VERSION), log);

        // client-side mod: never require it from the other side.
        // mods.toml has no clientSideOnly before Forge 50, so it goes through the display test.
        // IGNORESERVERONLY is a compile-time constant — javac inlines it, so nothing references
        // the holder class, which moved packages between 1.17.1 and 1.20.4.
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> FMLNetworkConstants.IGNORESERVERONLY,
                        (remote, isServer) -> true
                )
        );
    }

}

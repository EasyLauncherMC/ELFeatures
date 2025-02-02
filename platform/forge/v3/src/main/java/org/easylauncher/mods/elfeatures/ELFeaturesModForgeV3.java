package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.common.Mod;

@Log4j2
@Mod(value = "elfeatures")
public final class ELFeaturesModForgeV3 extends ELFeaturesModBase {

    public ELFeaturesModForgeV3() {
        super(String.format("ELFeatures/%s (Forge V3)", Constants.MOD_VERSION), log);
    }

}

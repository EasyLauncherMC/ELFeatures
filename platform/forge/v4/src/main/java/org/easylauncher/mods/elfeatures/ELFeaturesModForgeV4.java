package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.common.Mod;

@Log4j2
@Mod(value = "elfeatures")
public final class ELFeaturesModForgeV4 extends ELFeaturesModBase {

    public ELFeaturesModForgeV4() {
        super(String.format("ELFeatures/%s (Forge V4)", Constants.MOD_VERSION), log);
    }

}

package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.neoforged.fml.common.Mod;

@Log4j2
@Mod(value = "elfeatures")
public final class ELFeaturesModNeoForge extends ELFeaturesModBase {

    public ELFeaturesModNeoForge() {
        super(String.format("ELFeatures/%s (NeoForge)", Constants.MOD_VERSION), log);
    }

}

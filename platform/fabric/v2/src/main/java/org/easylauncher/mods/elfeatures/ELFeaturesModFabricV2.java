package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.ModInitializer;

@Log4j2
public final class ELFeaturesModFabricV2 extends ELFeaturesModBase implements ModInitializer {

	public ELFeaturesModFabricV2() {
		super(String.format("ELFeatures/%s (Fabric/Quilt V2)", Constants.MOD_VERSION), log);
	}

	@Override public void onInitialize() {}

}
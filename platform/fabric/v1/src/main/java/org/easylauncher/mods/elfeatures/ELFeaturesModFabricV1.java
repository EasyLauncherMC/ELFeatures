package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.ModInitializer;

@Log4j2
public final class ELFeaturesModFabricV1 extends ELFeaturesModBase implements ModInitializer {

	public ELFeaturesModFabricV1() {
		super(String.format("ELFeatures/%s (Fabric/Quilt V1)", Constants.MOD_VERSION), log);
	}

	@Override public void onInitialize() {}

}
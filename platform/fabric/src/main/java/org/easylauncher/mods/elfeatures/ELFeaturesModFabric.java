package org.easylauncher.mods.elfeatures;

import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.ModInitializer;

@Log4j2
public final class ELFeaturesModFabric extends ELFeaturesModBase implements ModInitializer {

	public ELFeaturesModFabric() {
		super(String.format("ELFeatures/%s (Fabric/Quilt)", Constants.MOD_VERSION), log);
	}

	@Override public void onInitialize() {}

}
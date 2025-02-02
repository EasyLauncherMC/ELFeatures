package org.easylauncher.mods.elfeatures;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class ELFeaturesService {

    @Getter(AccessLevel.PACKAGE)
    private static ELFeaturesService instance;

    private final ELFeaturesMod mod;

    static void initialize(ELFeaturesMod mod) {
        if (instance != null)
            throw new IllegalStateException("ELFeatures service is already initialized!");

        instance = new ELFeaturesService(mod);
    }

}

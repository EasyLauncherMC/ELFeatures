package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("SkinProvider$V1",                 "[4764,4998]")  // 26.1-snap-1 - 26.3-snap-1
                .add("SkinProvider$V2",                 "[4999,)")      // 26.3-snap-2 and newer
                .add("SkinProvider1$V1",                "[4764,4998]")  // 26.1-snap-1 - 26.3-snap-1
                .add("SkinProvider1$V2",                "[4999,)")      // 26.3-snap-2 and newer
                .add("SkinTextureDownloader",           "[4764,)")      // 26.1-snap-1 and newer
                .apply();
    }

}

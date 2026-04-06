package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("SkinProvider",                    "[4764,)")      // 26.1-snapshot-1 and newer
                .add("SkinProvider1",                   "[4764,)")      // 26.1-snapshot-1 and newer
                .add("SkinTextureDownloader",           "[4764,)")      // 26.1-snapshot-1 and newer
                .apply();
    }

}

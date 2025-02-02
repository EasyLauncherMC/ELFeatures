package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("HttpTexture",                     "(,4177]")      // 24w45a (1.21.4) and earlier
                .add("SkinManager$V1",                  "[3684,)")      // 23w42a (1.20.3) and newer
                .add("SkinManager1$V1",                 "[3567,3681]")  // 23w31a (1.20.2) - 23w41a (1.20.3)
                .add("SkinManager1$V2",                 "[3684,)")      // 23w42a (1.20.3) and newer
                .add("SkinTextureDownloader",           "[4178,)")      // 24w46a (1.21.4) and newer
                .apply();
    }

}

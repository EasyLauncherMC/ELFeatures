package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("HttpTexture",                     "(,4177]")      // 24w45a (1.21.4) and earlier
                .add("SkinManager$V1",                  "(,4537]")      // 25w33a (1.21.9) and earlier
                .add("SkinManager$V2",                  "[4539,4998]")  // 25w34a (1.21.9) - 26.3-snap-1
                .add("SkinManager$V3",                  "[4999,)")      // 26.3-snap-2 and newer
                .add("SkinManager1$V1",                 "(,4537]")      // 25w33a (1.21.9) and earlier
                .add("SkinManager1$V2",                 "[4539,4998]")  // 25w34a (1.21.9) - 26.3-snap-1
                .add("SkinManager1$V3",                 "[4999,)")      // 26.3-snap-2 and newer
                .add("SkinTextureDownloader",           "[4178,)")      // 24w46a (1.21.4) and newer
                .apply();
    }

}

package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: fix for multiplayer on 1.16.X
        createConstraintGroup("multiplayer")
                .add("MinecraftClient",                 "[2582,2586]")  // 1.16.4-pre2 - 1.16.5
                .apply();

        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("PlayerSkinProvider$V1",           "(,2567]")      // 1.16.1 and earlier
                .add("PlayerSkinProvider$V2",           "[2569,3465]")  // 20w27a (1.16.2) - 1.20.1
                .add("PlayerSkinProvider$V3",           "[3684,)")      // 23w42a (1.20.3) and newer
                .add("PlayerSkinProvider1$V1",          "(,2567]")      // 1.16.1 and earlier
                .add("PlayerSkinProvider1$V2",          "[3567,3681]")  // 23w31a (1.20.2) - 23w41a (1.20.3)
                .add("PlayerSkinProvider1$V3",          "[3684,)")      // 23w42a (1.20.3) and newer
                .add("PlayerSkinTexture$V1",            "[2217,2722]")  // 19w46b (1.15) - 1.17-rc1
                .add("PlayerSkinTexture$V2",            "[2723,4177]")  // 1.17-rc2 - 24w45a (1.21.4)
                .add("PlayerSkinTextureDownloader",     "[4178,)")      // 24w46a (1.21.4) and newer
                .add("SkinRemappingImageFilter",        "(,2216]")      // 19w46a (1.15) and earlier
                .apply();
    }

}

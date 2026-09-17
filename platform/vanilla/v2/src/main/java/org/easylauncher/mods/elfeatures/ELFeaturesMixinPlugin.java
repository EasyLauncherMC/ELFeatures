package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: journal of worlds and servers joined
        createConstraintGroup("activity")
                .add("ClientPlayNetworkHandler$V1",     "(,2521]")      // 20w13b (1.16) and earlier
                .add("ClientPlayNetworkHandler$V2",     "[2524,2526]")  // 20w14a (1.16) - 20w16a (1.16)
                .add("ClientPlayNetworkHandler$V3",     "[2529,)")      // 20w17a (1.16) and newer
                .add("ConnectScreen$V1",                "(,2713]")      // 21w18a (1.17) and earlier
                .add("ConnectScreen$V2",                "[2714,3443]")  // 21w19a (1.17) - 23w13a (1.19.4)
                .add("LevelStorageSession",             "[2524,)")      // 20w14a (1.16) and newer
                .add("MinecraftServer$V2",              "[2524,2526]")  // 20w14a (1.16) - 20w16a (1.16)
                .add("MinecraftServer$V3",              "[2529,)")      // 20w17a (1.16) and newer
                .apply();

        // --- feature: fix for multiplayer on 1.16.X
        createConstraintGroup("multiplayer")
                .add("MinecraftClient",                 "[2582,2586]")  // 1.16.4-pre2 - 1.16.5
                .apply();

        // --- feature: quick play into a world, native since 23w14a
        createConstraintGroup("quickplay")
                .add("IntegratedServerLoader$V5",       "[3080,3080]")  // 22w11a (1.19)
                .add("IntegratedServerLoader$V6",       "[3082,3443]")  // 22w12a (1.19) - 23w13a (1.19.4)
                .add("LevelStorage",                    "(,2526]")      // 20w16a (1.16) and earlier
                .add("MinecraftClient$V1",              "(,1932]")      // 19w07a (1.14) and earlier
                .add("MinecraftClient$V2",              "[1933,2526]")  // 19w08a (1.14) - 20w16a (1.16)
                .add("MinecraftClient$V3",              "[2529,2555]")  // 20w17a (1.16) - 20w22a (1.16)
                .add("MinecraftClient$V4",              "[2556,2975]")  // 1.16-pre1 - 1.18.2
                .add("MinecraftClient$V5",              "[3080,3080]")  // 22w11a (1.19)
                .add("MinecraftClient$V6",              "[3082,3443]")  // 22w12a (1.19) - 23w13a (1.19.4)
                .apply();

        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("PlayerSkinProvider$V1",           "(,2567]")      // 1.16.1 and earlier
                .add("PlayerSkinProvider$V2",           "[2569,3465]")  // 20w27a (1.16.2) - 1.20.1
                .add("PlayerSkinProvider$V3",           "[3684,)")      // 23w42a (1.20.3) and newer
                .add("PlayerSkinProvider1$V1",          "(,2567]")      // 1.16.1 and earlier
                .add("PlayerSkinProvider1$V2",          "[3567,3681]")  // 23w31a (1.20.2) - 23w41a (1.20.3)
                .add("PlayerSkinProvider1$V3",          "[3684,4537]")  // 23w42a (1.20.3) - 25w33a (1.21.9)
                .add("PlayerSkinProvider1$V4",          "[4539,)")      // 25w34a (1.21.9) and newer
                .add("PlayerSkinTexture$V1",            "[2205,2722]")  // 19w38a (1.15) - 1.17-rc1
                .add("PlayerSkinTexture$V2",            "[2723,4177]")  // 1.17-rc2 - 24w45a (1.21.4)
                .add("PlayerSkinTextureDownloader",     "[4178,)")      // 24w46a (1.21.4) and newer
                .add("SkinRemappingImageFilter",        "(,2204]")      // 19w37a (1.15) and earlier
                .apply();
    }

}

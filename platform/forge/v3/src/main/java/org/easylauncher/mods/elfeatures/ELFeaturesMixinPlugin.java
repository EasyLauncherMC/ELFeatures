package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.shared.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        // --- feature: journal of worlds and servers joined
        createConstraintGroup("activity")
                .add("ConnectScreen",                   "(,3337]")      // 1.19.4 and earlier
                .apply();

        // --- feature: quick play into a world, native since 1.20
        createConstraintGroup("quickplay")
                .add("Minecraft$V1",                    "(,2975]")      // 1.18.2 and earlier
                .add("Minecraft$V2",                    "[3105,3337]")  // 1.19 - 1.19.4
                .add("WorldOpenFlows",                  "[3105,3337]")  // 1.19 - 1.19.4
                .apply();

        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("SkinManager$V1",                  "(,3465]")      // 1.20.1 and earlier
                .add("SkinManager$V2",                  "[3684,3700]")  // 23w42a (1.20.3) - 1.20.4
                .add("SkinManager1$V1",                 "[3567,3681]")  // 23w31a (1.20.2) - 23w41a (1.20.3)
                .add("SkinManager1$V2",                 "[3684,3700]")  // 23w42a (1.20.3) - 1.20.4
                .apply();
    }

}

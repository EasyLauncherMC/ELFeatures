package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.core.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        super(MixinPluginCustomizer::useVersionJson);
    }

    @Override
    protected void registerConstraints() {
        // --- feature: skin/cape textures from EasyX
        createConstraintGroup("textures")
                .add("SkinManager$V1",                  "(,3465]")      // 1.20.1 and earlier
                .add("SkinManager$V2",                  "[3684,3700]")  // 23w42a (1.20.3) - 1.20.4
                .add("SkinManager1$V1",                 "[3567,3681]")  // 23w31a (1.20.2) - 23w41a (1.20.3)
                .add("SkinManager1$V2",                 "[3684,3700]")  // 23w42a (1.20.3) - 1.20.4
                .apply();
    }

}

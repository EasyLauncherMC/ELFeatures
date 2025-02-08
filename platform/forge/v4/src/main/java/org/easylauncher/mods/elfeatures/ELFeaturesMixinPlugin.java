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
                .add("HttpTexture",                     "(,4177]")      // 24w45a (1.21.4) and earlier
                .add("SkinTextureDownloader",           "[4178,)")      // 24w46a (1.21.4) and newer
                .apply();
    }

}

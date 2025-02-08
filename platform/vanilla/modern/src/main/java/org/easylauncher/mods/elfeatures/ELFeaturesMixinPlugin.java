package org.easylauncher.mods.elfeatures;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.easylauncher.mods.elfeatures.core.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        super(MixinPluginCustomizer::useVersionJson);
    }

    @Override
    protected void onLoad() {
        MixinExtrasBootstrap.init();
    }

    @Override
    protected void registerConstraints() {

    }

}

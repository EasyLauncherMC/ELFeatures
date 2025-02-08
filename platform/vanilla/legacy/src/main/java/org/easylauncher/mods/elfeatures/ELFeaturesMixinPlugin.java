package org.easylauncher.mods.elfeatures;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.easylauncher.mods.elfeatures.core.mixin.MixinPluginBase;

public final class ELFeaturesMixinPlugin extends MixinPluginBase {

    public ELFeaturesMixinPlugin() {
        super(customizer -> customizer.useRunningVersion(ELFeaturesTweaker.RUNNING_VERSION));
    }

    @Override
    protected void onLoad() {
        MixinExtrasBootstrap.init();
    }

    @Override
    protected void registerConstraints() {

    }

}

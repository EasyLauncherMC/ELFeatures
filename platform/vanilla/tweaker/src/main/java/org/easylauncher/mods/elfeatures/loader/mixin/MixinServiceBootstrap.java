package org.easylauncher.mods.elfeatures.loader.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public final class MixinServiceBootstrap implements IMixinServiceBootstrap {

    @Override
    public String getName() {
        return "ELFeatures";
    }

    @Override
    public String getServiceClassName() {
        return "org.easylauncher.mods.elfeatures.loader.mixin.MixinService";
    }

    @Override
    public void bootstrap() {
        // already done
    }

}

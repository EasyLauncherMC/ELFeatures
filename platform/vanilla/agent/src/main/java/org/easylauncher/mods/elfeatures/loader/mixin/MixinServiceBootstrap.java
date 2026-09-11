package org.easylauncher.mods.elfeatures.loader.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public final class MixinServiceBootstrap implements IMixinServiceBootstrap {

    @Override
    public String getName() {
        return "ELFeatures";
    }

    @Override
    public String getServiceClassName() {
        return MixinService.class.getName();
    }

    @Override
    public void bootstrap() {
        // the agent has already done everything there is to do before mixin starts
    }

}

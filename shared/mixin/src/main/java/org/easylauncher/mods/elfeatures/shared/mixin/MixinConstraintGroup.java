package org.easylauncher.mods.elfeatures.shared.mixin;

public interface MixinConstraintGroup {

    // expression examples: '*'; '[I]', '(I,I]', '[I,)' ...
    MixinConstraintGroup add(String mixinClassName, String expression);

    void apply();

}

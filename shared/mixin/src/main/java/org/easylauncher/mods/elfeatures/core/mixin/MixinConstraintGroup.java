package org.easylauncher.mods.elfeatures.core.mixin;

public interface MixinConstraintGroup {

    // expression examples: '*'; '[X]', '(X,Y]', '[X,)' ...
    MixinConstraintGroup add(String mixinClassName, String expression);

    void apply();

}

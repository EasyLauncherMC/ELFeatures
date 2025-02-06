package org.easylauncher.mods.elfeatures.shared.mixin;

public interface MixinConstraintGroup {

    // expression examples: '*'; '[X]', '(X,Y]', '[X,)' ...
    MixinConstraintGroup add(String mixinClassName, String expression);

    void apply();

}

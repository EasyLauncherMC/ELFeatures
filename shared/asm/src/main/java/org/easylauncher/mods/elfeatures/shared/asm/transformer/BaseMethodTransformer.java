package org.easylauncher.mods.elfeatures.shared.asm.transformer;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;

public abstract class BaseMethodTransformer extends BaseTransformer implements MethodTransformer {

    public BaseMethodTransformer(TransformerService transformerService) {
        super(transformerService);
    }

}

package org.easylauncher.mods.elfeatures.core.asm.transformer;

import org.easylauncher.mods.elfeatures.core.asm.TransformerService;

public abstract class BaseMethodTransformer extends BaseTransformer implements MethodTransformer {

    public BaseMethodTransformer(TransformerService transformerService) {
        super(transformerService);
    }

}

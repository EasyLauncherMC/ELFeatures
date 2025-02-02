package org.easylauncher.mods.elfeatures.shared.asm.transformer;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;

public abstract class BaseClassTransformer extends BaseTransformer implements ClassTransformer {

    public BaseClassTransformer(TransformerService transformerService) {
        super(transformerService);
    }

}

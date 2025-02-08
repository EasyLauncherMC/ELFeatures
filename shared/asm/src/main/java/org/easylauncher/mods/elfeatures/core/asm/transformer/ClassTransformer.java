package org.easylauncher.mods.elfeatures.core.asm.transformer;

import org.easylauncher.mods.elfeatures.core.asm.TransformerService;
import org.objectweb.asm.tree.ClassNode;

@FunctionalInterface
public interface ClassTransformer {

    ClassNode transform(ClassNode classNode);

    @FunctionalInterface
    interface Instantiator {

        ClassTransformer createInstance(TransformerService transformerService);

    }

}

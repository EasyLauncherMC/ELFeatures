package org.easylauncher.mods.elfeatures.shared.asm.transformer;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.objectweb.asm.tree.ClassNode;

@FunctionalInterface
public interface ClassTransformer {

    ClassNode transform(ClassNode classNode);

    @FunctionalInterface
    interface Instantiator {

        ClassTransformer createInstance(TransformerService transformerService);

    }

}

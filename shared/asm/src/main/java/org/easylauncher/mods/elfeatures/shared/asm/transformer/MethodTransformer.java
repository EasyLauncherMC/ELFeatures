package org.easylauncher.mods.elfeatures.shared.asm.transformer;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@FunctionalInterface
public interface MethodTransformer {

    MethodNode transform(ClassNode classNode, MethodNode methodNode);

    @FunctionalInterface
    interface Instantiator {

        MethodTransformer createInstance(TransformerService transformerService);

    }

}

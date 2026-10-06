package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerMinecraft {

    /** Quick play and the idle entry: {@link ActivityHooks#onClientTick} at the head of every client tick. */
    @TransformerTarget(
            className = "net.minecraft.client.Minecraft",
            methodNameSrg = "func_71407_l",
            methodNames = "runTick",
            methodDesc = "()V"
    )
    public static final class RunTick extends BaseMethodTransformer {

        public RunTick(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            // ActivityHooks.onClientTick(this)
            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(methodInsn(
                    INVOKESTATIC,
                    "org/easylauncher/mods/elfeatures/asm/ActivityHooks",
                    "onClientTick",
                    "(Ljava/lang/Object;)V"
            ));

            methodNode.instructions.insert(insnList);
            return methodNode;
        }

    }

}

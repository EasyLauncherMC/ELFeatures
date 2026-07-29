package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerIntegratedServer {

    public static final boolean OFFLINE_LAN_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.lan.offline"));

    @TransformerTarget(
            className = "net.minecraft.server.integrated.IntegratedServer",
            methodNameSrg = "func_71197_b",
            methodNames = {"init", "startServer"},
            methodDesc = "()Z"
    )
    public static final class Init extends BaseMethodTransformer {

        public Init(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            if (!OFFLINE_LAN_ENABLED)
                return methodNode;

            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (insnNode.getOpcode() != INVOKEVIRTUAL)
                    continue;

                // the owner isn't checked: javac emits the call against the receiver type,
                // which is IntegratedServer, while setOnlineMode is declared on MinecraftServer
                MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                if (!checkMethodDesc("(Z)V", methodInsnNode))
                    continue;

                // the SRG name is what the production client has, the plain one is for a deobfuscated environment
                if (!checkMethodName("func_71229_d", methodInsnNode) && !"setOnlineMode".equals(methodInsnNode.name))
                    continue;

                // setOnlineMode(true) -> setOnlineMode(false)
                AbstractInsnNode argumentInsnNode = methodInsnNode.getPrevious();
                if (argumentInsnNode != null && argumentInsnNode.getOpcode() == ICONST_1)
                    methodNode.instructions.set(argumentInsnNode, new InsnNode(ICONST_0));

                break;
            }

            return methodNode;
        }

    }

}

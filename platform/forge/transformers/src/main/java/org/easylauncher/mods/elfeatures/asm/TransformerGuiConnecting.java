package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerGuiConnecting {

    /**
     * The activity journal: the address of a connection no server list entry stands behind, as {@code --server} makes,
     * kept for the join it leads to. This constructor is the one taking a bare host and port.
     */
    @TransformerTarget(
            className = "net.minecraft.client.multiplayer.GuiConnecting",
            methodNames = "<init>",
            methodDesc = "(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V"
    )
    public static final class Init extends BaseMethodTransformer {

        public Init(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (insnNode.getOpcode() != RETURN)
                    continue;

                // ActivityJournalWriter.connecting(host, port)
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(ALOAD, 3));
                insnList.add(new VarInsnNode(ILOAD, 4));
                insnList.add(methodInsn(
                        INVOKESTATIC,
                        "org/easylauncher/mods/elfeatures/activity/ActivityJournalWriter",
                        "connecting",
                        "(Ljava/lang/String;I)V"
                ));

                methodNode.instructions.insertBefore(insnNode, insnList);
            }

            return methodNode;
        }

    }

    /**
     * The server-list constructor: the one {@code --server} does not use. The host and port live on the
     * {@code ServerData}; {@link ActivityHooks#connectingFromServerData} reads them.
     */
    @TransformerTarget(
            className = "net.minecraft.client.multiplayer.GuiConnecting",
            methodNames = "<init>",
            methodDesc = "(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ServerData;)V"
    )
    public static final class InitFromServerData extends BaseMethodTransformer {

        public InitFromServerData(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (insnNode.getOpcode() != RETURN)
                    continue;

                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(ALOAD, 3));
                insnList.add(methodInsn(
                        INVOKESTATIC,
                        "org/easylauncher/mods/elfeatures/asm/ActivityHooks",
                        "connectingFromServerData",
                        "(Ljava/lang/Object;)V"
                ));
                methodNode.instructions.insertBefore(insnNode, insnList);
            }

            return methodNode;
        }

    }

}

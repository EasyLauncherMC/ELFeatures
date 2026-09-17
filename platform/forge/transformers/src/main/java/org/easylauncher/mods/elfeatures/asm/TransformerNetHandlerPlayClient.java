package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerNetHandlerPlayClient {

    /** The activity journal, 1.7.10 – 1.8.9: {@link ActivityHooks#onJoinGame} once the join packet is handled. */
    @TransformerTarget(
            className = "net.minecraft.client.network.NetHandlerPlayClient",
            methodNameSrg = "func_147282_a",
            methodNames = "handleJoinGame",
            methodDesc = "(Lnet/minecraft/network/play/server/S01PacketJoinGame;)V"
    )
    public static final class HandleJoinGameV1 extends BaseMethodTransformer {

        public HandleJoinGameV1(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            return insertOnJoinGame(methodNode);
        }

    }

    /** The activity journal, 1.9 – 1.12.2: the packet is {@code SPacketJoinGame} now. */
    @TransformerTarget(
            className = "net.minecraft.client.network.NetHandlerPlayClient",
            methodNameSrg = "func_147282_a",
            methodNames = "handleJoinGame",
            methodDesc = "(Lnet/minecraft/network/play/server/SPacketJoinGame;)V"
    )
    public static final class HandleJoinGameV2 extends BaseMethodTransformer {

        public HandleJoinGameV2(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            return insertOnJoinGame(methodNode);
        }

    }

    // ActivityHooks.onJoinGame() before every return
    private static MethodNode insertOnJoinGame(MethodNode methodNode) {
        for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
            if (insnNode.getOpcode() != RETURN)
                continue;

            methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(
                    INVOKESTATIC,
                    "org/easylauncher/mods/elfeatures/asm/ActivityHooks",
                    "onJoinGame",
                    "()V",
                    false
            ));
        }

        return methodNode;
    }

}

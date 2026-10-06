package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

public final class TransformerNetClientHandler {

    /** The activity journal, 1.5.2 – 1.6.4: {@link ActivityHooks#onJoinGame} once the login packet is handled. */
    @TransformerTarget(
            className = "net.minecraft.client.multiplayer.NetClientHandler",
            methodNameSrg = "func_72455_a",
            methodNames = "handleLogin",
            methodDesc = "(Lnet/minecraft/network/packet/Packet1Login;)V"
    )
    public static final class HandleLogin extends BaseMethodTransformer {

        public HandleLogin(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            return TransformerNetHandlerPlayClient.insertOnJoinGame(methodNode);
        }

    }

}

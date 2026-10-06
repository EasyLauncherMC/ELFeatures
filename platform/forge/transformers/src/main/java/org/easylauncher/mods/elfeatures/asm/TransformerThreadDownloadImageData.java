package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Sends the thread downloading a skin or a cape through LegacyTextureUrls: 1.5-1.7.2 ask skins.minecraft.net by name.
 */
public final class TransformerThreadDownloadImageData {

    @TransformerTarget(
            className = "net.minecraft.client.renderer.ThreadDownloadImageData$1",
            methodNameSrg = "run",
            methodDesc = "()V"
    )
    public static final class Run extends ResolveUrl {

        public Run(TransformerService transformerService) {
            super(transformerService);
        }

    }

    // what the SRG names of 1.6 call the same anonymous thread
    @TransformerTarget(
            className = "net.minecraft.client.renderer.ThreadDownloadImageDataINNER1",
            methodNameSrg = "run",
            methodDesc = "()V"
    )
    public static final class RunInner extends ResolveUrl {

        public RunInner(TransformerService transformerService) {
            super(transformerService);
        }

    }

    // the thread of 1.5, a class of its own rather than one inside ThreadDownloadImageData
    @TransformerTarget(
            className = "net.minecraft.client.renderer.ThreadDownloadImage",
            methodNameSrg = "run",
            methodDesc = "()V"
    )
    public static final class RunThread extends ResolveUrl {

        public RunThread(TransformerService transformerService) {
            super(transformerService);
        }

    }

    private abstract static class ResolveUrl extends BaseMethodTransformer {

        ResolveUrl(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (!checkMethod(insnNode, INVOKESPECIAL, "java/net/URL.<init> (Ljava/lang/String;)V"))
                    continue;

                // new URL(url) -> new URL(LegacyTextureUrls.resolve(url))
                methodNode.instructions.insertBefore(insnNode, methodInsn(
                        INVOKESTATIC,
                        "org/easylauncher/mods/elfeatures/texture/LegacyTextureUrls",
                        "resolve",
                        "(Ljava/lang/String;)Ljava/lang/String;"
                ));
            }

            return methodNode;
        }

    }

}

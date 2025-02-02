package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerSkinManager {

    @TransformerTarget(
            className = "net.minecraft.client.resources.SkinManager",
            methodNameSrg = "func_152788_a",
            methodNames = "loadSkinFromCache",
            methodDesc = "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;"
    )
    public static final class LoadSkinFromCache extends BaseMethodTransformer {

        public LoadSkinFromCache(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            List<Runnable> pendingInjections = new ArrayList<>();
            int step = 0;

            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (step == 0 && insnNode.getOpcode() == ALOAD && ((VarInsnNode) insnNode).var == 0) {
                    InsnList insnList = new InsnList();

                    // get the ELTP instance
                    insnList.add(new MethodInsnNode(
                            INVOKESTATIC,
                            "org/easylauncher/mods/elfeatures/ELFeaturesMod",
                            "texturesProvider",
                            "()Lorg/easylauncher/mods/elfeatures/textures/TexturesProvider;",
                            false
                    ));

                    // load GameProfile
                    insnList.add(new VarInsnNode(ALOAD, 1));

                    pendingInjections.add(() -> methodNode.instructions.insertBefore(insnNode, insnList));
                    step++;
                    continue;
                }

                if (step == 1 && checkMethod(insnNode, INVOKEINTERFACE, "com/google/common/cache/LoadingCache.getUnchecked (Ljava/lang/Object;)Ljava/lang/Object;")) {
                    InsnList insnList = new InsnList();

                    // load textures via ELTP
                    insnList.add(new MethodInsnNode(
                            INVOKEVIRTUAL,
                            "org/easylauncher/mods/elfeatures/textures/TexturesProvider",
                            "loadTexturesMap",
                            "(Lcom/mojang/authlib/GameProfile;Ljava/util/Map;)Ljava/util/Map;",
                            false
                    ));

                    pendingInjections.add(() -> methodNode.instructions.insert(insnNode, insnList));
                    step++;
                    break;
                }
            }

            if (step == 2)
                pendingInjections.forEach(Runnable::run);

            return methodNode;
        }

    }

}

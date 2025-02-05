package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public final class TransformerSkinManager$3 {

    @TransformerTarget(
            className = "net.minecraft.client.resources.SkinManager$3",
            methodNameSrg = "run",
            methodDesc = "()V"
    )
    public static final class Run extends BaseMethodTransformer {

        public Run(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            int putAllsCount = 0;
            FieldInsnNode gameProfileFieldInsn = null;

            for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                if (gameProfileFieldInsn == null && insnNode.getOpcode() == GETFIELD && checkFieldDesc("Lcom/mojang/authlib/GameProfile;", insnNode)) {
                    AbstractInsnNode previous = insnNode.getPrevious();
                    if (previous != null && previous.getOpcode() == ALOAD && ((VarInsnNode) previous).var == 0) {
                        FieldInsnNode cast = (FieldInsnNode) insnNode;
                        gameProfileFieldInsn = new FieldInsnNode(GETFIELD, cast.owner, cast.name, cast.desc);
                    }
                }

                if (checkMethod(insnNode, INVOKEINTERFACE, "java/util/Map.putAll (Ljava/util/Map;)V")) {
                    putAllsCount++;
                    continue;
                }

                if (checkMethod(insnNode, INVOKEVIRTUAL, "java/util/HashMap.putAll (Ljava/util/Map;)V")) {
                    putAllsCount++;
                    continue;
                }

                if (checkMethod(insnNode, INVOKESTATIC, "net/minecraft/client/Minecraft.func_71410_x ()Lnet/minecraft/client/Minecraft;")) {
                    if (putAllsCount != 2)
                        continue;

                    InsnList insnList = new InsnList();

                    // get the ELTP instance
                    insnList.add(new MethodInsnNode(
                            INVOKESTATIC,
                            "org/easylauncher/mods/elfeatures/ELFeaturesMod",
                            "authlibEasyxTexturesProvider",
                            "()Lorg/easylauncher/mods/elfeatures/texture/provider/AuthlibEasyxTexturesProvider;",
                            false
                    ));

                    // load GameProfile instance from the field
                    insnList.add(new VarInsnNode(ALOAD, 0));
                    insnList.add(gameProfileFieldInsn != null ? gameProfileFieldInsn : new FieldInsnNode(
                            GETFIELD,
                            "net/minecraft/client/resources/SkinManager$3",
                            "field_152799_a",
                            "Lcom/mojang/authlib/GameProfile;"
                    ));

                    // load Map instance from the variable
                    insnList.add(new VarInsnNode(ALOAD, 1));

                    // load textures via ELTP
                    insnList.add(new MethodInsnNode(
                            INVOKEVIRTUAL,
                            "org/easylauncher/mods/elfeatures/texture/provider/AuthlibEasyxTexturesProvider",
                            "loadTexturesMap",
                            "(Lcom/mojang/authlib/GameProfile;Ljava/util/Map;)Ljava/util/Map;",
                            false
                    ));

                    // save Map instance to the variable
                    insnList.add(new TypeInsnNode(CHECKCAST, "java/util/HashMap"));
                    insnList.add(new VarInsnNode(ASTORE, 1));

                    methodNode.instructions.insertBefore(insnNode, insnList);
                }
            }

            return methodNode;
        }

    }

}

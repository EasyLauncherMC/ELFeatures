package org.easylauncher.mods.elfeatures.asm;

import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.core.asm.TransformerService;
import org.easylauncher.mods.elfeatures.core.asm.TransformerTarget;
import org.easylauncher.mods.elfeatures.core.asm.transformer.BaseClassTransformer;
import org.easylauncher.mods.elfeatures.core.asm.transformer.BaseMethodTransformer;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Log4j2
public final class TransformerImageBufferDownload {

    private static final String FIELD_NAME_SCALE_FACTOR = "elfeatures$scaleFactor";

    @TransformerTarget(className = "net.minecraft.client.renderer.ImageBufferDownload")
    public static final class ClassStructure extends BaseClassTransformer {

        public ClassStructure(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public ClassNode transform(ClassNode classNode) {
            classNode.visitField(ACC_PRIVATE, FIELD_NAME_SCALE_FACTOR, "I", null, null).visitEnd();
            return classNode;
        }

    }

    @TransformerTarget(
            className = "net.minecraft.client.renderer.ImageBufferDownload",
            methodNameSrg = "func_78432_a",
            methodDesc = "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;"
    )
    public static final class ParseUserSkin extends BaseMethodTransformer {

        public ParseUserSkin(TransformerService transformerService) {
            super(transformerService);
        }

        @Override
        public MethodNode transform(ClassNode classNode, MethodNode methodNode) {
            InsnList instructions = methodNode.instructions;
            for (int i = 0; i < instructions.size(); i++) {
                AbstractInsnNode insnNode = instructions.get(i);
                if (insnNode.getOpcode() == ALOAD && checkOpcodes(instructions, i + 1, DUP, GETFIELD, ICONST_2, IMUL, PUTFIELD)) {
                    log.info("Seems that ImageBufferDownload already has HD skins support.");
                    log.info("Patch by ELFeatures will be skipped here...");
                    return methodNode;
                }
            }

            List<Runnable> pendingInjections = new ArrayList<>();
            int step = 0;

            for (AbstractInsnNode insnNode : instructions.toArray()) {
                // step #1: pass only valid texture images
                if (step == 0 && insnNode.getOpcode() == IFNONNULL) {
                    InsnList insnList = new InsnList();

                    // pass only valid texture images
                    insnList.add(new MethodInsnNode(
                            INVOKESTATIC,
                            "org/easylauncher/mods/elfeatures/texture/TexturesInspector",
                            "passValidTextureImage",
                            "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
                            false
                    ));

                    pendingInjections.add(() -> instructions.insertBefore(insnNode, insnList));
                    step++;
                    continue;
                }

                // step #2: compute scale factor
                if (step == 1 && checkField(insnNode, "net/minecraft/client/renderer/ImageBufferDownload.field_78436_b : I")) {
                    InsnList insnList = new InsnList();

                    // load this
                    insnList.add(new VarInsnNode(ALOAD, 0));

                    // load BufferedImage instance
                    insnList.add(new VarInsnNode(ALOAD, 1));

                    // compute scale factor
                    insnList.add(new MethodInsnNode(
                            INVOKESTATIC,
                            "org/easylauncher/mods/elfeatures/texture/TexturesInspector",
                            "computeTextureScale",
                            "(Ljava/awt/image/BufferedImage;)I",
                            false
                    ));

                    // save scale factor
                    insnList.add(new FieldInsnNode(
                            PUTFIELD,
                            "net/minecraft/client/renderer/ImageBufferDownload",
                            FIELD_NAME_SCALE_FACTOR,
                            "I"
                    ));

                    AbstractInsnNode targetNode = insnNode.getPrevious().getPrevious();
                    pendingInjections.add(() -> instructions.insert(targetNode, insnList));
                    step++;
                    break;
                }
            }

            if (step == 2) {
                pendingInjections.forEach(Runnable::run);

                // step #3: replace ICONST_4 to BIPUSH 4
                for (AbstractInsnNode insnNode : instructions.toArray())
                    if (insnNode.getOpcode() == ICONST_4)
                        instructions.set(insnNode, new IntInsnNode(BIPUSH, 4));

                // step #4: multiply BIPUSH 4 with scaleFactor
                for (AbstractInsnNode insnNode : instructions.toArray()) {
                    if (insnNode.getOpcode() == BIPUSH && insnNode.getNext() != null && insnNode.getNext().getOpcode() != ILOAD) {
                        InsnList insnList = new InsnList();

                        // load scale factor
                        insnList.add(new VarInsnNode(ALOAD, 0));
                        insnList.add(new FieldInsnNode(
                                GETFIELD,
                                "net/minecraft/client/renderer/ImageBufferDownload",
                                FIELD_NAME_SCALE_FACTOR,
                                "I"
                        ));

                        // multiple ints
                        insnList.add(new InsnNode(IMUL));

                        instructions.insert(insnNode, insnList);
                    }
                }
            }

            return methodNode;
        }
    }

}

package org.easylauncher.mods.elfeatures;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.easylauncher.mods.elfeatures.asm.TransformerImageBufferDownload;
import org.easylauncher.mods.elfeatures.asm.TransformerSkinManager;
import org.easylauncher.mods.elfeatures.asm.TransformerSkinManager$3;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.ClassTransformer;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.MethodTransformer;
import org.easylauncher.mods.elfeatures.shared.asm.BytecodeWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public final class ELFeaturesTransformer implements IClassTransformer {

    private static final Remapper REMAPPER;
    private static final TransformerService TRANSFORMER_SERVICE;

    @Override
    public byte[] transform(String obfName, String name, byte[] bytes) {
        if (bytes == null || bytes.length == 0 || !TRANSFORMER_SERVICE.containsName(name))
            return bytes;

        // bytes -> class node
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

        // transform class node
        classNode = TRANSFORMER_SERVICE.transformClass(classNode, name);

        // transform methods
        List<MethodNode> transformedMethods = new ArrayList<>();
        for (MethodNode methodNode : classNode.methods) {
            String methodName = REMAPPER.mapMethodName(classNode.name, methodNode.name, methodNode.desc);
            String methodDesc = REMAPPER.mapMethodDesc(methodNode.desc);
            transformedMethods.add(TRANSFORMER_SERVICE.transformMethod(classNode, name, methodNode, methodName, methodDesc));
        }

        // apply transformed methods
        classNode.methods.clear();
        classNode.methods.addAll(transformedMethods);

        // class node -> bytes
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        BytecodeWriter.saveClassBytecode(name, bytes, false);
        return BytecodeWriter.saveClassBytecode(name, writer.toByteArray(), true);
    }

    static {
        REMAPPER = FMLDeobfuscatingRemapper.INSTANCE;
        TRANSFORMER_SERVICE = new TransformerService(
                REMAPPER,
                new ClassTransformer.Instantiator[] {
                        TransformerImageBufferDownload.ClassStructure::new,
                },
                new MethodTransformer.Instantiator[] {
                        TransformerImageBufferDownload.ParseUserSkin::new,
                        TransformerSkinManager.LoadSkinFromCache::new,
                        TransformerSkinManager$3.Run::new,
                }
        );
    }

}

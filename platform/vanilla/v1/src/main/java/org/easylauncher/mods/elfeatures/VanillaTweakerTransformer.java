package org.easylauncher.mods.elfeatures;

import org.easylauncher.mods.elfeatures.launch.ELFeaturesTweaker;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * The way in up to 1.5.2, where the launch wrapper starts the game in a class loader of its own and takes a single
 * tweak class, the vanilla one. That one is made to run {@link ELFeaturesTweaker} after itself, so the mod comes up
 * inside the wrapper's loader as it does under OptiFine.
 */
final class VanillaTweakerTransformer implements ClassFileTransformer {

    private static final String LAUNCH_WRAPPER = "net.minecraft.launchwrapper.Launch";
    private static final String VANILLA_TWEAKER = "net/minecraft/launchwrapper/VanillaTweaker";

    private static final String INJECT_METHOD = "injectIntoClassLoader";
    private static final String INJECT_DESCRIPTOR = "(Lnet/minecraft/launchwrapper/LaunchClassLoader;)V";

    /** Whether the JVM was told to run the launch wrapper rather than the game itself. */
    static boolean isGameWrapped() {
        String command = System.getProperty("sun.java.command", "");
        return command.equals(LAUNCH_WRAPPER) || command.startsWith(LAUNCH_WRAPPER + ' ');
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer
    ) {
        if (!VANILLA_TWEAKER.equals(className))
            return null;

        ClassNode node = new ClassNode();
        new ClassReader(classfileBuffer).accept(node, 0);

        for (MethodNode method : node.methods)
            if (INJECT_METHOD.equals(method.name) && INJECT_DESCRIPTOR.equals(method.desc))
                for (AbstractInsnNode instruction : method.instructions.toArray())
                    if (instruction.getOpcode() == Opcodes.RETURN)
                        method.instructions.insertBefore(instruction, tweakerCall());

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    // new ELFeaturesTweaker().injectIntoClassLoader(classLoader)
    private static InsnList tweakerCall() {
        String tweaker = Type.getInternalName(ELFeaturesTweaker.class);

        InsnList call = new InsnList();
        call.add(new TypeInsnNode(Opcodes.NEW, tweaker));
        call.add(new InsnNode(Opcodes.DUP));
        call.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, tweaker, "<init>", "()V", false));
        call.add(new VarInsnNode(Opcodes.ALOAD, 1));
        call.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, tweaker, INJECT_METHOD, INJECT_DESCRIPTOR, false));
        return call;
    }

}

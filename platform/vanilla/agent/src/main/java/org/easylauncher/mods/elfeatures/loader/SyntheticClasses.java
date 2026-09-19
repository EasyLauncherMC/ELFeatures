package org.easylauncher.mods.elfeatures.loader;

import lombok.CustomLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Puts the classes mixin makes up as it works — the argument holders of {@code @ModifyArgs} and their like —
 * somewhere the game's own class loader will find them.
 *
 * <p>Under a launch wrapper mixin hands these over when the loader comes asking for a class it couldn't find.
 * A java agent is never asked: the JVM consults a transformer only for classes it already has bytes for. So
 * every class a freshly transformed one refers to and nothing on disk answers for is generated here and
 * appended to the system class path, which happens well before the code that needs it runs.
 */
@CustomLog
final class SyntheticClasses {

    private final Instrumentation instrumentation;
    private final Set<String> defined = new HashSet<>();
    private Path directory;

    SyntheticClasses(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    /** Lays down whatever [classBytes] came to refer to that isn't on the class path. */
    synchronized void defineReferencedBy(byte[] classBytes) {
        for (String internalName : referencedTypes(classBytes)) {
            if (!defined.add(internalName))
                continue;

            if (ClassLoader.getSystemResource(internalName + ".class") != null)
                continue;

            try {
                byte[] generated = MixinPipeline.generate(internalName);
                if (generated != null) append(internalName, generated);
            } catch (Throwable cause) {
                log.error("Couldn't lay down the generated class '" + internalName + "'!", cause);
            }
        }
    }

    /** Every type named anywhere in [classBytes] — the remapper is asked about each one exactly once. */
    private static Set<String> referencedTypes(byte[] classBytes) {
        final Set<String> types = new HashSet<String>();

        new ClassReader(classBytes).accept(new ClassRemapper(new ClassNode(), new Remapper(Opcodes.ASM9) {
            @Override
            public String map(String internalName) {
                types.add(internalName);
                return internalName;
            }
        }), 0);

        return types;
    }

    private void append(String internalName, byte[] classBytes) throws IOException {
        if (directory == null)
            this.directory = Files.createTempDirectory("elfeatures-generated");

        Path jarPath = directory.resolve(internalName.replace('/', '.') + ".jar");

        try (OutputStream output = Files.newOutputStream(jarPath)) {
            JarOutputStream jar = new JarOutputStream(output);
            jar.putNextEntry(new JarEntry(internalName + ".class"));
            jar.write(classBytes);
            jar.closeEntry();
            jar.finish();
        }

        instrumentation.appendToSystemClassLoaderSearch(new JarFile(jarPath.toFile()));
        log.debug("Laid the generated class '{}' down at '{}'", internalName, jarPath);
    }

}

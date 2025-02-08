package org.easylauncher.mods.elfeatures.core.asm;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.easylauncher.mods.elfeatures.core.asm.TransformerService.DEBUG_ENABLED;

@Log4j2
public final class BytecodeWriter {

    private static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.home")).resolve("elfeatures-transform");

    public static byte[] saveClassBytecode(String className, byte[] bytes, boolean transformed) {
        if (!DEBUG_ENABLED)
            return bytes;

        int lastDotIndex = className.lastIndexOf('.');
        String fileName = lastDotIndex != -1 ? className.substring(lastDotIndex + 1) : className;

        if (transformed)
            fileName += "_transformed";

        Path filePath = OUTPUT_DIR.resolve(fileName + ".class");
        try {
            if (!Files.isDirectory(filePath.getParent()))
                Files.createDirectories(filePath.getParent());

            Files.write(filePath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            log.error("Couldn't save bytecode of class '{}' (transformed: {})", className, transformed);
            log.error(ex);
        }

        return bytes;
    }

}

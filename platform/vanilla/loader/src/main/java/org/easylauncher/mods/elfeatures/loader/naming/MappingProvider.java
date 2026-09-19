package org.easylauncher.mods.elfeatures.loader.naming;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Reads the intermediary mappings the launcher put on disk and named in {@code elfeatures.mappings}.
 *
 * <p>Handed over rather than downloaded: the launcher already fetches the artifact as part of the installation,
 * and a game started without a network still has to run. An empty tree is a valid answer — the releases whose
 * classes are not obfuscated need no mappings at all.
 */
@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MappingProvider {

    private static final String MAPPINGS_ENTRY = "mappings/mappings.tiny";
    private static final String MAPPINGS_PROPERTY = "elfeatures.mappings";

    public static MappingTree loadMappings() {
        MemoryMappingTree mappings = new MemoryMappingTree();

        String path = System.getProperty(MAPPINGS_PROPERTY);
        if (path == null) {
            log.warn("No '{}' was given, mixins will not be mapped!", MAPPINGS_PROPERTY);
            return mappings;
        }

        long time = System.currentTimeMillis();

        try (JarFile jar = new JarFile(path)) {
            JarEntry entry = jar.getJarEntry(MAPPINGS_ENTRY);
            if (entry == null) {
                log.warn("'{}' carries no '{}', mixins will not be mapped!", path, MAPPINGS_ENTRY);
                return mappings;
            }

            try (
                    InputStream stream = jar.getInputStream(entry);
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(reader)
            ) {
                read(bufferedReader, new FilteringMappingVisitor(mappings));
            }

            log.info("Loading mappings took {} ms.", System.currentTimeMillis() - time);
        } catch (Exception cause) {
            log.error("Couldn't load the mappings from '" + path + "'!", cause);
        }

        return mappings;
    }

    private static void read(BufferedReader reader, FilteringMappingVisitor visitor) throws IOException {
        MappingFormat format = resolveMappingsFormat(reader);
        if (ELFeaturesMod.DEBUG_ENABLED)
            log.info("[Debug] Resolved mappings format: " + format);

        switch (format) {
            case TINY_FILE:
                Tiny1FileReader.read(reader, visitor);
                break;
            case TINY_2_FILE:
                Tiny2FileReader.read(reader, visitor);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported mapping format: " + format);
        }
    }

    private static MappingFormat resolveMappingsFormat(BufferedReader reader) throws IOException {
        try {
            reader.mark(4096);
            return MappingReader.detectFormat(reader);
        } finally {
            reader.reset();
        }
    }

}

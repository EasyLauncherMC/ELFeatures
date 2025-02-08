package org.easylauncher.mods.elfeatures.loader.mapping;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MappingProvider {

    public static MappingTree loadMappings() {
        MemoryMappingTree mappings = new MemoryMappingTree();

        URLConnection mappingsConnection = openMappings();
        if (mappingsConnection == null) {
            log.warn("Couldn't open mappings to read, mixins will not be mapped!");
            return mappings;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mappingsConnection.getInputStream()))) {
            long time = System.currentTimeMillis();
            FilteringMappingVisitor mappingFilter = new FilteringMappingVisitor(mappings);

            MappingFormat format = resolveMappingsFormat(reader);
            if (ELFeaturesMod.DEBUG_ENABLED)
                log.info("[Debug] Resolved mappings format: " + format);

            switch (format) {
                case TINY_FILE:
                    Tiny1FileReader.read(reader, mappingFilter);
                    break;
                case TINY_2_FILE:
                    Tiny2FileReader.read(reader, mappingFilter);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported mapping format: " + format);
            }

            if (ELFeaturesMod.LOGGING_ENABLED) {
                log.info("Loading mappings took {} ms.", System.currentTimeMillis() - time);
            }
        } catch (Exception ex) {
            log.error("Couldn't load mappings!", ex);
        }

        return mappings;
    }

    private static URLConnection openMappings() {
        try {
            URL mappingsUrl = MappingProvider.class.getClassLoader().getResource("mappings/mappings.tiny");
            return mappingsUrl != null ? mappingsUrl.openConnection() : null;
        } catch (IOException ex) {
            log.error("Couldn't open mappings file!", ex);
            return null;
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

package org.easylauncher.mods.elfeatures.locator;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
final class ELFeaturesJarFinder {

    // Forge 1.13.2 - 1.16.5
    static Path findWithJarProtocol(ClassLoader classLoader) {
        URL resource = classLoader.getResource("org/easylauncher/mods/elfeatures/ELFeaturesMod.class");
        if (resource == null) {
            log.error("Couldn't find ELFeaturesMod class resource!");
            return null;
        }

        if (!"jar".equals(resource.getProtocol())) {
            log.error("ELFeaturesMod class resource URL has an unexpected protocol: '{}'", resource);
            return null;
        }

        String rawPath = resource.getPath();
        if (!rawPath.startsWith("file:/") || !rawPath.contains("!")) {
            log.error("ELFeaturesMod class resource URL has an unexpected path: '{}'", rawPath);
            return null;
        }

        Path jarPath = Paths.get(rawPath.substring(6, rawPath.indexOf('!')).replace('/', File.separatorChar));
        if (Files.isRegularFile(jarPath))
            return jarPath;

        log.error("ELFeatures JAR file not found!");
        return null;
    }

}

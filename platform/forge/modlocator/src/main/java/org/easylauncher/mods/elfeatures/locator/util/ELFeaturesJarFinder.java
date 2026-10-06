package org.easylauncher.mods.elfeatures.locator.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ELFeaturesJarFinder {

    private static final String ELFEATURES_JAR_PATH_PROPERTY = "elfeatures.jarPath";

    public static Path findJarPath() {
        String rawPath = System.getProperty(ELFEATURES_JAR_PATH_PROPERTY);
        if (rawPath == null)
            return null;

        if (rawPath.charAt(0) == '"' && rawPath.charAt(rawPath.length() - 1) == '"')
            rawPath = rawPath.substring(1, rawPath.length() - 1);

        return Paths.get(rawPath);
    }

}

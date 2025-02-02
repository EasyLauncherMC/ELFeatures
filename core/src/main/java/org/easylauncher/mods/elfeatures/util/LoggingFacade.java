package org.easylauncher.mods.elfeatures.util;

@FunctionalInterface
public interface LoggingFacade {

    void log(String message, Object... args);

}

package org.easylauncher.mods.elfeatures.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;

final class Log4j2LoggerAdapter implements LoggerAdapter {

    private final Logger logger;

    Log4j2LoggerAdapter(Class<?> type) {
        this.logger = LogManager.getLogger(type);
    }

    @Override
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    @Override
    public void info(String message, Object... args) {
        if (ELFeaturesMod.LOGGING_ENABLED) {
            logger.info(message, args);
        }
    }

    @Override
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    @Override
    public void error(String message, Object... args) {
        logger.error(message, args);
    }

}

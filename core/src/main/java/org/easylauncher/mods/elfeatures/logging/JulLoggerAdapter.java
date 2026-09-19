package org.easylauncher.mods.elfeatures.logging;

import org.easylauncher.mods.elfeatures.ELFeaturesMod;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

final class JulLoggerAdapter implements LoggerAdapter {

    private static final Logger LOGGER = createLogger();

    private final String source;

    JulLoggerAdapter(Class<?> type) {
        this.source = type.getName();
    }

    @Override
    public void debug(String message, Object... args) {
        log(Level.FINE, message, args);
    }

    @Override
    public void info(String message, Object... args) {
        if (ELFeaturesMod.LOGGING_ENABLED) {
            log(Level.INFO, message, args);
        }
    }

    @Override
    public void warn(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log(Level.SEVERE, message, args);
    }

    private void log(Level level, String message, Object[] args) {
        if (!LOGGER.isLoggable(level))
            return;

        StringBuilder builder = new StringBuilder();
        int from = 0, used = 0;
        for (int at; used < args.length && (at = message.indexOf("{}", from)) >= 0; from = at + 2)
            builder.append(message, from, at).append(args[used++]);

        builder.append(message, from, message.length());

        Object last = used < args.length ? args[args.length - 1] : null;
        LOGGER.logp(level, source, null, builder.toString(), last instanceof Throwable ? (Throwable) last : null);
    }

    private static Logger createLogger() {
        Logger logger = Logger.getLogger("ELFeatures");

        // FML of 1.5-1.6 takes the root logger's handlers away and writes the log through one of its own
        Logger fmlLogger = LogManager.getLogManager().getLogger("ForgeModLoader");
        if (fmlLogger != null) logger.setParent(fmlLogger);

        return logger;
    }

}

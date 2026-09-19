package org.easylauncher.mods.elfeatures.logging;

/**
 * The mod's log: the game's log4j from 1.7 on, java.util.logging before it, where the game ships none.
 *
 * <p>Messages take log4j's {@code {}} placeholders, and a {@link Throwable} left after them is logged as the cause.
 */
public interface LoggerAdapter {

    void debug(String message, Object... args);

    /** Written only with {@code elfeatures.logging.enabled}, which the launcher sets. */
    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Object... args);

    static LoggerAdapter of(Class<?> type) {
        try {
            Class.forName("org.apache.logging.log4j.LogManager", false, LoggerAdapter.class.getClassLoader());
            return new Log4j2LoggerAdapter(type);
        } catch (ClassNotFoundException ignored) {
            return new JulLoggerAdapter(type);
        }
    }

}

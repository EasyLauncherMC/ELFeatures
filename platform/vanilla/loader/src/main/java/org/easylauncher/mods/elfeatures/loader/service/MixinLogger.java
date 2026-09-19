package org.easylauncher.mods.elfeatures.loader.service;

import lombok.CustomLog;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// based on source code: https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/impl/launch/knot/MixinLogger.java

@CustomLog
public final class MixinLogger extends LoggerAdapterAbstract {

    private static final Map<String, ILogger> LOGGER_MAP = new ConcurrentHashMap<>();

    public static ILogger get(String name) {
        return LOGGER_MAP.computeIfAbsent(name, MixinLogger::new);
    }

    private MixinLogger(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "ELFeatures Mixin Logger";
    }

    @Override
    public void catching(Level level, Throwable throwable) {
        log(level, "Catching " + throwable.toString(), throwable);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        if (!shouldLog(level))
            return;

        Throwable exception = null;

        if (params != null && params.length > 0) {
            if (message == null) {
                if (params[0] instanceof Throwable) {
                    exception = (Throwable) params[0];
                }
            } else {
                // emulate Log4J's {} tokens and \ escapes
                StringBuilder stringBuilder = new StringBuilder(message.length() + 20);
                int paramIdx = 0;
                boolean escaped = false;

                for (int i = 0, max = message.length(); i < max; i++) {
                    char c = message.charAt(i);

                    if (escaped) {
                        stringBuilder.append(c);
                        escaped = false;
                    } else if (c == '\\' && i + 1 < max) {
                        escaped = true;
                    } else if (c == '{' && i + 1 < max && message.charAt(i + 1) == '}' && paramIdx < params.length) { // unescaped {} with matching param idx
                        Object param = params[paramIdx++];

                        if (param == null) {
                            stringBuilder.append("null");
                        } else if (param.getClass().isArray()) {
                            String val = Arrays.deepToString(new Object[] { param });
                            stringBuilder.append(val, 1, val.length() - 1);
                        } else {
                            stringBuilder.append(param);
                        }

                        i++; // skip over }
                    } else {
                        stringBuilder.append(c);
                    }
                }

                message = stringBuilder.toString();

                if (paramIdx < params.length && params[params.length - 1] instanceof Throwable) {
                    exception = (Throwable) params[params.length - 1];
                }
            }
        }

        log(level, message, exception);
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (!shouldLog(level))
            return;

        // an argument rather than the pattern, since the message is formatted already
        switch (level) {
            case TRACE:
                log.info("[Trace] {}", message, throwable);
                break;
            case DEBUG:
                log.info("[Debug] {}", message, throwable);
                break;
            case INFO:
                log.info("{}", message, throwable);
                break;
            case WARN:
                log.warn("{}", message, throwable);
                break;
            default:
                log.error("{}", message, throwable);
                break;
        }
    }

    @Override
    public <T extends Throwable> T throwing(T throwable) {
        log(Level.ERROR, "Throwing " + throwable, throwable);
        return throwable;
    }

    private static boolean shouldLog(Level level) {
        if (!ELFeaturesMod.LOGGING_ENABLED)
            return level == Level.ERROR || level == Level.FATAL;

        if (!ELFeaturesMod.DEBUG_ENABLED)
            return level != Level.TRACE && level != Level.DEBUG;

        return true;
    }

}

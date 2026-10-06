package org.easylauncher.mods.elfeatures.loader.service;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The blackboard mixin keeps its own state on, which every other implementation puts in the host's.
 *
 * <p>The two mixin ships are a launch wrapper's and a mod launcher's, and neither class is even loadable here —
 * mixin walks past both and lands on this one.
 *
 * <p>The {@code META-INF/services} entry naming it lives in every platform module rather than here: the shadow
 * jar is built off the compile class path, which carries a project's classes but not its resources.
 */
public final class GlobalPropertyService implements IGlobalPropertyService {

    private final Map<IPropertyKey, Object> properties = new ConcurrentHashMap<IPropertyKey, Object>();

    @Override
    public IPropertyKey resolveKey(String name) {
        return new Key(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T) properties.get(key);
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        Object value = properties.get(key);
        return value != null ? (T) value : defaultValue;
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object value = properties.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static final class Key implements IPropertyKey {

        private final String name;

        @Override
        public String toString() {
            return name;
        }

    }

}

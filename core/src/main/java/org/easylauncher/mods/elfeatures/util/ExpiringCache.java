package org.easylauncher.mods.elfeatures.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A value per key, loaded once and dropped after going unread for a while — guava's cache did this here, and up to 1.5.2
 * the game ships no guava. Each key loads under a lock of its own, so one slow request holds up no other; a {@code null} is kept
 * like any other value.
 */
public final class ExpiringCache<K, V> {

    private final ConcurrentMap<K, Entry> entries = new ConcurrentHashMap<>();
    private final long expiryNanos;
    private final Function<K, V> loader;

    public ExpiringCache(long expiry, TimeUnit unit, Function<K, V> loader) {
        this.expiryNanos = unit.toNanos(expiry);
        this.loader = loader;
    }

    public V get(K key) {
        long now = System.nanoTime();
        entries.values().removeIf(entry -> now - entry.accessedAt > expiryNanos);
        return entries.computeIfAbsent(key, ignored -> new Entry(now)).get(key, now);
    }

    private final class Entry {

        private volatile long accessedAt;
        private boolean loaded;
        private V value;

        private Entry(long createdAt) {
            this.accessedAt = createdAt;
        }

        private synchronized V get(K key, long now) {
            this.accessedAt = now;

            if (!loaded) {
                this.value = loader.apply(key);
                this.loaded = true;
            }

            return value;
        }

    }

}

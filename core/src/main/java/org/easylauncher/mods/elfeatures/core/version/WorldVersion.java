package org.easylauncher.mods.elfeatures.core.version;

import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public final class WorldVersion implements MinecraftVersion {

    public static final int MIN_WORLD_VERSION = 1913;

    private final int version;

    public static Optional<WorldVersion> parse(String rawVersion) {
        try {
            int version = Integer.parseInt(rawVersion);
            if (version >= MIN_WORLD_VERSION) { // 18w47b (1.14)
                return Optional.of(new WorldVersion(version));
            }
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }

    @Override
    public Type getType() {
        return Type.WORLD_VERSION;
    }

    @Override
    public boolean isComparableWith(MinecraftVersion other) {
        return other instanceof WorldVersion;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        if (other instanceof WorldVersion) {
            WorldVersion otherVersion = (WorldVersion) other;
            if (equals(otherVersion))
                return 0;

            return Integer.compare(otherVersion.version, version);
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        WorldVersion that = (WorldVersion) o;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version);
    }

    @Override
    public String toString() {
        return String.valueOf(version);
    }

}

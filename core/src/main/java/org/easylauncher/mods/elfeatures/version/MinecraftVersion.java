package org.easylauncher.mods.elfeatures.version;

import java.util.Optional;

import static org.easylauncher.mods.elfeatures.version.WorldVersion.MIN_WORLD_VERSION;

public interface MinecraftVersion extends Comparable<MinecraftVersion> {

    static Optional<MinecraftVersion> ofProfile(String profile) {
        MinecraftVersion version = NamedVersion.parse(profile).orElse(null);
        if (version != null)
            return Optional.of(version);

        version = SnapshotVersion.parse(profile).orElse(null);
        return version != null ? Optional.of(version) : Optional.empty();
    }

    static Optional<MinecraftVersion> ofWorldVersion(int worldVersion) {
        return worldVersion >= MIN_WORLD_VERSION
                ? Optional.of(new WorldVersion(worldVersion))
                : Optional.empty();
    }

    static Optional<MinecraftVersion> parse(String rawVersion) {
        MinecraftVersion version = ofProfile(rawVersion).orElse(null);
        if (version != null)
            return Optional.of(version);

        version = WorldVersion.parse(rawVersion).orElse(null);
        return version != null ? Optional.of(version) : Optional.empty();
    }

    Type getType();

    boolean isComparableWith(MinecraftVersion other);

    default boolean isEqual(MinecraftVersion other) {
        if (!isComparableWith(other))
            throw new UnsupportedOperationException("Incomparable versions!");

        return equals(other);
    }

    default boolean isNewerThan(MinecraftVersion other) {
        return isNewerThan(other, true);
    }

    default boolean isNewerThanOrEqual(MinecraftVersion other) {
        return isNewerThan(other, false);
    }

    default boolean isNewerThan(MinecraftVersion other, boolean strict) {
        if (!isComparableWith(other))
            throw new UnsupportedOperationException("Incomparable versions!");

        return strict ? (compareTo(other) < 0) : (compareTo(other) <= 0);
    }

    default boolean isOlderThan(MinecraftVersion other) {
        return isOlderThan(other, true);
    }

    default boolean isOlderThanOrEqual(MinecraftVersion other) {
        return isOlderThan(other, false);
    }

    default boolean isOlderThan(MinecraftVersion other, boolean strict) {
        if (!isComparableWith(other))
            throw new UnsupportedOperationException("Incomparable versions!");

        return strict ? (compareTo(other) > 0) : (compareTo(other) >= 0);
    }

    enum Type {

        RELEASE,
        RELEASE_CANDIDATE,
        PRE_RELEASE,
        SNAPSHOT,
        WORLD_VERSION,
        ;

        public static Optional<Type> fromSuffix(String suffix) {
            if (suffix == null || suffix.isEmpty())
                return Optional.empty();

            switch (suffix.toLowerCase()) {
                case "rc":
                    return Optional.of(Type.RELEASE_CANDIDATE);
                case "pre":
                    return Optional.of(Type.PRE_RELEASE);
                default:
                    return Optional.empty();
            }
        }

        public boolean isRelease() {
            return this == RELEASE;
        }

        public boolean isReleaseCandidate() {
            return this == RELEASE_CANDIDATE;
        }

        public boolean isPreRelease() {
            return this == PRE_RELEASE;
        }

        public boolean isSnapshot() {
            return this == SNAPSHOT;
        }

        public boolean isWorldVersion() {
            return this == WORLD_VERSION;
        }

        public String toNameSuffix(int number) {
            switch (this) {
                case RELEASE_CANDIDATE:
                    return "-rc" + number;
                case PRE_RELEASE:
                    return "-pre" + number;
                default:
                    return "";
            }
        }

    }

}

package org.easylauncher.mods.elfeatures.version;

import java.util.Optional;

public interface MinecraftVersion extends Comparable<MinecraftVersion> {

    static Optional<MinecraftVersion> parse(String rawVersion) {
        MinecraftVersion version = NamedVersion.parse(rawVersion).orElse(null);
        if (version != null)
            return Optional.of(version);

        version = SnapshotVersion.parse(rawVersion).orElse(null);
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
        if (!isComparableWith(other))
            throw new UnsupportedOperationException("Incomparable versions!");

        return compareTo(other) < 0;
    }

    default boolean isOlderThan(MinecraftVersion other) {
        if (!isComparableWith(other))
            throw new UnsupportedOperationException("Incomparable versions!");

        return compareTo(other) > 0;
    }

    enum Type {

        RELEASE,
        RELEASE_CANDIDATE,
        PRE_RELEASE,
        SNAPSHOT,
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

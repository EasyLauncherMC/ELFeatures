package org.easylauncher.mods.elfeatures.version;

import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public final class SnapshotVersion implements MinecraftVersion {

    private static final Pattern REGEX = Pattern.compile("^(\\d{2})w(\\d{2})(.+)$");

    private final int year;
    private final int week;
    private final String suffix;

    public static Optional<SnapshotVersion> parse(String rawVersion) {
        if (rawVersion == null || rawVersion.length() < 6)
            return Optional.empty();

        Matcher matcher = REGEX.matcher(rawVersion);
        if (!matcher.matches())
            return Optional.empty();

        try {
            int year = Integer.parseInt(matcher.group(1));
            if (year < 13)
                return Optional.empty();

            int week = Integer.parseInt(matcher.group(2));
            if (week < 1 || week > 54)
                return Optional.empty();

            String suffix = matcher.group(3);
            if (suffix == null || suffix.isEmpty())
                return Optional.empty();

            return Optional.of(new SnapshotVersion(year, week, suffix));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Type getType() {
        return Type.SNAPSHOT;
    }

    @Override
    public boolean isComparableWith(MinecraftVersion other) {
        return other instanceof SnapshotVersion;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        if (other instanceof SnapshotVersion) {
            SnapshotVersion otherVersion = (SnapshotVersion) other;
            if (equals(otherVersion))
                return 0;

            int compareResult = Integer.compare(otherVersion.year, year);
            if (compareResult != 0)
                return compareResult;

            compareResult = Integer.compare(otherVersion.week, week);
            if (compareResult != 0)
                return compareResult;

            return suffix.compareTo(otherVersion.suffix);
        }

        return getType().compareTo(other.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SnapshotVersion that = (SnapshotVersion) o;
        return year == that.year
                && week == that.week
                && Objects.equals(suffix, that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, week, suffix);
    }

    @Override
    public String toString() {
        return String.format("%02dw%02d%s", year, week, suffix);
    }

}

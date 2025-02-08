package org.easylauncher.mods.elfeatures.core.version;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamedVersion implements MinecraftVersion {

    private static final Pattern REGEX = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d))?(?:-(?<suffix>\\D+)(?<number>\\d+))?$");

    @Getter
    private final Type type;
    private final int major;
    private final int minor;
    private final int patch;
    private final int number;

    public static Optional<NamedVersion> parse(String rawVersion) {
        if (rawVersion == null || rawVersion.length() < 3)
            return Optional.empty();

        Matcher matcher = REGEX.matcher(rawVersion);
        if (!matcher.matches())
            return Optional.empty();

        try {
            int major = Integer.parseInt(matcher.group("major"));
            int minor = Integer.parseInt(matcher.group("minor"));
            if (major < 1 || minor < 0)
                return Optional.empty();

            String rawPatch = matcher.group("patch");
            int patch = rawPatch != null ? Integer.parseInt(rawPatch) : 0;
            if (patch < 0)
                return Optional.empty();

            String suffix = matcher.group("suffix");
            String rawNumber = matcher.group("number");
            if (suffix == null || rawNumber == null)
                return Optional.of(new NamedVersion(Type.RELEASE, major, minor, patch, 0));

            Optional<Type> type = Type.fromSuffix(suffix);
            if (!type.isPresent())
                return Optional.empty();

            int number = Integer.parseInt(rawNumber);
            if (number < 1)
                return Optional.empty();

            return Optional.of(new NamedVersion(type.get(), major, minor, patch, number));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public String getName() {
        String name = major + "." + minor;
        if (patch > 0) name += "." + patch;
        return name;
    }

    @Override
    public boolean isComparableWith(MinecraftVersion other) {
        return other instanceof NamedVersion;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        if (other instanceof NamedVersion) {
            NamedVersion otherVersion = (NamedVersion) other;
            if (equals(otherVersion))
                return 0;

            int compareResult = Integer.compare(otherVersion.major, major);
            if (compareResult != 0)
                return compareResult;

            compareResult = Integer.compare(otherVersion.minor, minor);
            if (compareResult != 0)
                return compareResult;

            compareResult = Integer.compare(otherVersion.patch, patch);
            if (compareResult != 0)
                return compareResult;

            compareResult = type.compareTo(otherVersion.type);
            if (compareResult != 0)
                return compareResult;

            return Integer.compare(otherVersion.number, number);
        }

        return getType().compareTo(other.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        NamedVersion that = (NamedVersion) o;
        return major == that.major
                && minor == that.minor
                && patch == that.patch
                && number == that.number
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, major, minor, patch, number);
    }

    @Override
    public String toString() {
        return String.format("%s%s", getName(), type.toNameSuffix(number));
    }

}

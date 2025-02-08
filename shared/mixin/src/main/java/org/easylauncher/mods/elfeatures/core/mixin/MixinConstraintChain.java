package org.easylauncher.mods.elfeatures.core.mixin;

import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.core.version.MinecraftVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Log4j2
public final class MixinConstraintChain {

    private final List<MixinConstraint> constraints;

    public MixinConstraintChain() {
        this.constraints = new ArrayList<>();
    }

    public void append(MixinConstraint constraint) {
        this.constraints.add(constraint);
    }

    public boolean passBoth(MinecraftVersion minecraftVersion, MinecraftVersion worldVersion) {
        return pass(minecraftVersion) || pass(worldVersion);
    }

    public void forEach(Consumer<MixinConstraint> consumer) {
        this.constraints.forEach(consumer);
    }

    private boolean pass(MinecraftVersion version) {
        if (!isComparableWith(version))
            return false;

        for (MixinConstraint constraint : constraints)
            if (!constraint.pass(version))
                return false;

        return true;
    }

    private boolean isComparableWith(MinecraftVersion version) {
        if (version == null)
            return false;

        long comparableWith = constraints.stream()
                .filter(constraint -> constraint.isComparableWith(version))
                .count();

        if (comparableWith == 0L)
            return false;

        if (comparableWith == constraints.size())
            return true;

        log.warn("A mixin constraint chain contains constraints for different version types!");
        return false;
    }

}

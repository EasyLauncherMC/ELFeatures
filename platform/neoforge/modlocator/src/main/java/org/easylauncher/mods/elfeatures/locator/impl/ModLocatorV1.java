package org.easylauncher.mods.elfeatures.locator.impl;

import net.neoforged.fml.loading.moddiscovery.AbstractJarFileModLocator;
import org.easylauncher.mods.elfeatures.locator.util.ELFeaturesJarFinder;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public final class ModLocatorV1 extends AbstractJarFileModLocator {

    @Override
    public String name() {
        return null;
    }

    @Override
    public Stream<Path> scanCandidates() {
        return Stream.ofNullable(ELFeaturesJarFinder.findJarPath());
    }

    @Override
    public void initArguments(Map<String, ?> map) {}

}

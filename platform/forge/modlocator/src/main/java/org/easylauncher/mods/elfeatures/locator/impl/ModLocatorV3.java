package org.easylauncher.mods.elfeatures.locator.impl;

import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.easylauncher.mods.elfeatures.locator.util.ELFeaturesJarFinder;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class ModLocatorV3 extends AbstractModProvider implements IModLocator {

    @Override
    public String name() {
        return null;
    }

    @Override
    public List<ModFileOrException> scanMods() {
        Path jarPath = ELFeaturesJarFinder.findJarPath();
        return jarPath != null ? List.of(createMod(jarPath)) : Collections.emptyList();
    }

}

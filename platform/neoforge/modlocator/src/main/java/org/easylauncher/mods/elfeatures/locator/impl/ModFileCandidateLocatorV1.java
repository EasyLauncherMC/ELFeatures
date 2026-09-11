package org.easylauncher.mods.elfeatures.locator.impl;

import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.easylauncher.mods.elfeatures.locator.util.ELFeaturesJarFinder;

import java.nio.file.Path;

public final class ModFileCandidateLocatorV1 implements IModFileCandidateLocator {

    @Override
    public String name() {
        return null;
    }

    @Override
    public int getPriority() {
        return HIGHEST_SYSTEM_PRIORITY;
    }

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        Path jarPath = ELFeaturesJarFinder.findJarPath();
        if (jarPath != null) {
            pipeline.addPath(jarPath, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.ERROR);
        }
    }

}

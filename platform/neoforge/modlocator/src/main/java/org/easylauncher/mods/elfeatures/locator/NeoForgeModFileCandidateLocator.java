package org.easylauncher.mods.elfeatures.locator;

import lombok.extern.log4j.Log4j2;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;

import java.util.Map;

@Log4j2
public final class NeoForgeModFileCandidateLocator implements IModFileCandidateLocator {

    private static final Map<String, CompatibilityTester> IMPLEMENTATIONS = Map.of(
            "ModFileCandidateLocatorV1", () -> Class.forName("net.neoforged.neoforgespi.locating.IModFileCandidateLocator")
    );

    private final IModFileCandidateLocator implementation;

    public NeoForgeModFileCandidateLocator() {
        this.implementation = resolveImplementation();
    }

    @Override
    public String name() {
        return "ELFeatures NeoForge ModFileCandidateLocator";
    }

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        if (implementation != null) {
            implementation.findCandidates(context, pipeline);
        }
    }

    @Override
    public int getPriority() {
        return implementation != null ? implementation.getPriority() : DEFAULT_PRIORITY;
    }

    private static IModFileCandidateLocator resolveImplementation() {
        for (var implementation : IMPLEMENTATIONS.entrySet()) {
            try {
                implementation.getValue().testCompatibility();
                String className = "org.easylauncher.mods.elfeatures.locator.impl." + implementation.getKey();
                Class<?> IModCandidateProvider = Class.forName(className);
                return (IModFileCandidateLocator) IModCandidateProvider.getConstructor().newInstance();
            } catch (Throwable ignored) {}
        }

        log.error("Couldn't find compatible implementation of IModFileCandidateLocator!");
        return null;
    }

    @FunctionalInterface
    private interface CompatibilityTester {

        void testCompatibility() throws Throwable;

    }

}

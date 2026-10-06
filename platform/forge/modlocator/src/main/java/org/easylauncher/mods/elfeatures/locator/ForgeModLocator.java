package org.easylauncher.mods.elfeatures.locator;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
public final class ForgeModLocator implements IModLocator {

    private static final Map<String, CompatibilityTester> IMPLEMENTATIONS = Map.of(
            "ModLocatorV3", () -> Class.forName("net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider").getDeclaredMethod("createMod", Path.class),
            "ModLocatorV2", () -> Class.forName("net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator"),
            "ModLocatorV1", () -> Class.forName("net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator")
    );

    private final IModLocator implementation;

    public ForgeModLocator() {
        this.implementation = resolveImplementation();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List scanMods() {
        return implementation != null ? implementation.scanMods() : Collections.emptyList();
    }

    @Override
    public String name() {
        return "ELFeatures Forge ModLocator";
    }

    @Override
    public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
        if (implementation != null) {
            implementation.scanFile(modFile, pathConsumer);
        }
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
        if (implementation != null) {
            implementation.initArguments(arguments);
        }
    }

    @Override
    public boolean isValid(IModFile modFile) {
        return implementation != null && implementation.isValid(modFile);
    }

    private static IModLocator resolveImplementation() {
        for (var implementation : IMPLEMENTATIONS.entrySet()) {
            try {
                implementation.getValue().testCompatibility();
                String className = "org.easylauncher.mods.elfeatures.locator.impl." + implementation.getKey();
                Class<?> IModLocator = Class.forName(className);
                return (IModLocator) IModLocator.getConstructor().newInstance();
            } catch (Throwable ignored) {}
        }

        log.error("Couldn't find compatible implementation of IModLocator!");
        return null;
    }

    @FunctionalInterface
    private interface CompatibilityTester {

        void testCompatibility() throws Throwable;

    }

}

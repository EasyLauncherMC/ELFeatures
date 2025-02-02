package org.easylauncher.mods.elfeatures.locator;

import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
@Log4j2
public class ELFeaturesModLocator extends AbstractJarFileLocator {

    @Override
    public String name() {
        return "ELFeatures classpath locator";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List scanMods() {
        Path jarPath = ELFeaturesJarFinder.findWithJarProtocol(getClass().getClassLoader());
        if (jarPath == null)
            return Collections.EMPTY_LIST;

        ModFile modFile = instantiateModFile(jarPath, this);
        if (modFile == null) {
            log.error("Couldn't instantiate ModFile using both known methods!");
            return Collections.EMPTY_LIST;
        }

        if (!registerModFile(modFile)) {
            log.error("Couldn't register ModFile instance!");
            return Collections.EMPTY_LIST;
        }

        return Collections.singletonList(modFile);
    }

    @Override
    public void initArguments(Map<String, ?> map) {}

    @SuppressWarnings({"rawtypes", "unchecked", "JavaReflectionMemberAccess"})
    private boolean registerModFile(ModFile modFile) {
        try {
            Class<?> AJFLocator = Class.forName("net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator");
            Method AJFLocator$createFileSystem = null;

            for (Method method : AJFLocator.getDeclaredMethods()) {
                if (method.getName().equals("createFileSystem")) {
                    AJFLocator$createFileSystem = method;
                    break;
                }
            }

            if (AJFLocator$createFileSystem == null) {
                log.error("AbstractJarFileLocator#createFileSystem not found!");
                return false;
            }

            Field AJFLocator$modJars = AJFLocator.getDeclaredField("modJars");
            Map modJars = (Map) AJFLocator$modJars.get(this);

            FileSystem fileSystem = (FileSystem) AJFLocator$createFileSystem.invoke(this, modFile);
            if (fileSystem == null) {
                log.error("Couldn't create mod FileSystem!");
                return false;
            }

            modJars.put(modFile, fileSystem);
            return true;
        } catch (Throwable ex) {
            log.error("Couldn't register ModFile via reflection!");
            log.error(ex);
            return false;
        }
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "JavaReflectionInvocation"})
    private static ModFile instantiateModFile(Path jarPath, AbstractJarFileLocator modLocator) {
        // 1.13.2 - 1.15.1
        try {
            Constructor<?> ModFile$init;

            try {
                Class<?> IModLocator = Class.forName("net.minecraftforge.fml.loading.moddiscovery.IModLocator");
                ModFile$init = ModFile.class.getConstructor(Path.class, IModLocator);
            } catch (Throwable ignored) {
                Class<?> IModLocator = Class.forName("net.minecraftforge.forgespi.locating.IModLocator");
                ModFile$init = ModFile.class.getConstructor(Path.class, IModLocator);
            }

            return (ModFile) ModFile$init.newInstance(jarPath, modLocator);
        } catch (Throwable ignored) {
        }

        // 1.15.2 - 1.16.5
        try {
            Class<?> IModLocator = Class.forName("net.minecraftforge.forgespi.locating.IModLocator");
            Method ModFile$newFMLInstance = ModFile.class.getMethod("newFMLInstance", Path.class, IModLocator);
            return (ModFile) ModFile$newFMLInstance.invoke(null, jarPath, modLocator);
        } catch (Throwable ignored) {
        }

        log.error("Couldn't find compatible strategy to instantiate an ModFile instance!");
        return null;
    }

}

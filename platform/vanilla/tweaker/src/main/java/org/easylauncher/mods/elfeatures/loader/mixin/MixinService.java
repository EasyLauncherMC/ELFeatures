package org.easylauncher.mods.elfeatures.loader.mixin;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.easylauncher.mods.elfeatures.ELFeaturesTweakerBase;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesClassTransformer;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.easylauncher.mods.elfeatures.util.UrlUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

public final class MixinService implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {

    private static IMixinTransformer transformer;

    private final LaunchClassLoader launchClassLoader;
    private final ReEntranceLock lock;

    public MixinService() {
        this.launchClassLoader = ELFeaturesTweakerBase.singleton().getLaunchClassLoader();
        this.lock = new ReEntranceLock(1);
    }

    @Override
    public String getName() {
        return ELFeaturesTweakerBase.singleton().getModName();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void prepare() { }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.PREINIT;
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory) {
            transformer = ((IMixinTransformerFactory) internal).createTransformer();
        }
    }

    @Override
    public void init() { }

    @Override
    public void beginPhase() { }

    @Override
    public void checkEnv(Object bootSource) { }

    @Override
    public ReEntranceLock getReEntranceLock() {
        return lock;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    @Override
    public IClassTracker getClassTracker() {
        return this;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleURI(UrlUtil.LOADER_CODE_SOURCE.toUri());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return launchClassLoader.getResourceAsStream(name);
    }

    @Override
    public String getSideName() {
        return "CLIENT";
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return null;
    }

    @Override
    public ILogger getLogger(String name) {
        return MixinLogger.get(name);
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        ClassReader reader = new ClassReader(getClassBytes(name, runTransformers));
        ClassNode node = new ClassNode();
        reader.accept(node, readerFlags);
        return node;
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return launchClassLoader.loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, launchClassLoader);
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, launchClassLoader);
    }

    @Override
    public void registerInvalidClass(String className) { }

    @Override
    public boolean isClassLoaded(String className) {
        return false;
    }

    @Override
    public String getClassRestrictions(String className) {
        return "";
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ITransformer> getDelegatedTransformers() {
        return Collections.emptyList();
    }

    @Override
    public void addTransformerExclusion(String name) { }

    public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        String transformedName = name.replace('/', '.');

        if (ELFeaturesClassTransformer.isMinecraftClass(transformedName)) {
            String unmappedName = ELFeaturesMixinBootstrap.getMixinRemapper().map(transformedName.replace('.', '/'));
            if (unmappedName != null) {
                unmappedName = unmappedName.replace('/', '.');
                transformedName = unmappedName;
            }
        }

        byte[] classBytes = launchClassLoader.getClassBytes(transformedName);
        if (classBytes == null)
            classBytes = getSystemClassBytes(transformedName);

        if (runTransformers) {
            for (IClassTransformer transformer : launchClassLoader.getTransformers()) {
                if (transformer instanceof Proxy)
                    continue; // skip mixin as per method contract

                classBytes = transformer.transform(name, transformedName, classBytes);
            }
        }

        if (classBytes != null)
            return classBytes;

        throw new ClassNotFoundException(name);
    }

    private static byte[] getSystemClassBytes(String name) throws IOException {
        InputStream resource = ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class");
        if (resource == null)
            return null;

        byte[] buffer = new byte[1024];
        int read;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((read = resource.read(buffer)) != -1)
            outputStream.write(buffer, 0, read);

        return outputStream.toByteArray();
    }

    public static IMixinTransformer getTransformer() {
        assert transformer != null;
        return transformer;
    }

}

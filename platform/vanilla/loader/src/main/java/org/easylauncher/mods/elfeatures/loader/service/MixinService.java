package org.easylauncher.mods.elfeatures.loader.service;

import lombok.Setter;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.easylauncher.mods.elfeatures.loader.MixinPipeline;
import org.easylauncher.mods.elfeatures.loader.naming.MixinClassRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * Mixin's whole view of the world when it is running without a mod loader.
 *
 * <p>A class is read as a resource off the loader the game itself came from and the defining is left to whoever
 * asked — the system loader under the agent, the wrapper's own loader under the tweaker. What a mixin names is
 * intermediary, what lies on the class path is obfuscated — the remapper stands between the two.
 */
public final class MixinService implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {

    private static final MixinClassRemapper CLASS_REMAPPER = new MixinClassRemapper();

    @Setter
    private static ClassLoader classSource = ClassLoader.getSystemClassLoader();
    private static IMixinTransformer transformer;

    private final ReEntranceLock lock = new ReEntranceLock(1);

    @Override
    public String getName() {
        return "ELFeatures";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void prepare() {
    }

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
    public void init() {
    }

    @Override
    public void beginPhase() {
    }

    @Override
    public void checkEnv(Object bootSource) {
    }

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
    public IAdviceProvider getAdviceProvider() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public IFeatureValidator getFeatureValidator() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleURI(modJarUri());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return classSource.getResourceAsStream(name);
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
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(getClassBytes(name));

        // the game's own classes are read under the names they already have; a mixin is written against
        // intermediary ones and has to be rewritten before mixin measures it against its target
        ClassVisitor visitor = MixinPipeline.isMinecraftClass(name.replace('.', '/'))
                ? node
                : new ClassRemapper(node, CLASS_REMAPPER);

        reader.accept(visitor, readerFlags);
        return node;
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, classSource);
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return findClass(name, initialize);
    }

    @Override
    public void registerInvalidClass(String className) {
    }

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
    public void addTransformerExclusion(String name) {
    }

    public static IMixinTransformer getTransformer() {
        return transformer;
    }

    private byte[] getClassBytes(String name) throws ClassNotFoundException, IOException {
        String internalName = name.replace('.', '/');

        IRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        if (remapper != null && MixinPipeline.isMinecraftClass(internalName)) {
            String obfuscatedName = remapper.map(internalName);
            if (obfuscatedName != null) {
                internalName = obfuscatedName;
            }
        }

        InputStream resource = classSource.getResourceAsStream(internalName + ".class");
        if (resource == null)
            throw new ClassNotFoundException(name);

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];

            int read;
            while ((read = resource.read(buffer)) != -1)
                bytes.write(buffer, 0, read);

            return bytes.toByteArray();
        } finally {
            resource.close();
        }
    }

    private static URI modJarUri() {
        try {
            URL location = MixinService.class.getProtectionDomain().getCodeSource().getLocation();
            return "jar".equals(location.getProtocol())
                    ? ((JarURLConnection) location.openConnection()).getJarFileURL().toURI()
                    : location.toURI();
        } catch (Exception cause) {
            throw new IllegalStateException("The mod can't tell which JAR it was loaded from!", cause);
        }
    }

}

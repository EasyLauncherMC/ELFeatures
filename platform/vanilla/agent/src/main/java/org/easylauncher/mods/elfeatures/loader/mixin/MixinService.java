package org.easylauncher.mods.elfeatures.loader.mixin;

import org.easylauncher.mods.elfeatures.loader.ELFeaturesClassTransformer;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
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
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * Mixin's whole view of the world when it is running as a java agent.
 *
 * <p>There is no class loader of ours to ask: the game is on the system class path, so a class is read as a
 * resource and the JVM is left to define it. What a mixin names is intermediary, what lies on the class path is
 * obfuscated — the remapper stands between the two.
 */
public final class MixinService implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {

    private static final MixinClassRemapper CLASS_REMAPPER = new MixinClassRemapper();

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
        return new ContainerHandleURI(agentJarUri());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return ClassLoader.getSystemResourceAsStream(name);
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
        ClassVisitor visitor = ELFeaturesClassTransformer.isMinecraftClass(name.replace('.', '/'))
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
        return Class.forName(name, initialize, ClassLoader.getSystemClassLoader());
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

    /**
     * The bytes of a class as they lie on the class path, under whichever name the running release has for it.
     *
     * <p>The bytes are the untouched ones from the jar even where OptiFine patches the class — what
     * {@code transform} is handed is the patched shape, and mixin only asks here about classes it hasn't been
     * handed.
     */
    private byte[] getClassBytes(String name) throws ClassNotFoundException, IOException {
        String internalName = name.replace('.', '/');

        IRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        if (remapper != null && ELFeaturesClassTransformer.isMinecraftClass(internalName)) {
            String obfuscatedName = remapper.map(internalName);
            if (obfuscatedName != null) {
                internalName = obfuscatedName;
            }
        }

        InputStream resource = ClassLoader.getSystemResourceAsStream(internalName + ".class");
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

    private static URI agentJarUri() {
        try {
            return MixinService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (Exception cause) {
            throw new IllegalStateException("The agent can't tell which jar it was loaded from!", cause);
        }
    }

}

package org.easylauncher.mods.elfeatures.core.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.core.version.MinecraftVersion;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

@Log4j2
public abstract class MixinPluginBase implements IMixinConfigPlugin {

    private final Map<String, List<MixinConstraintChain>> constraintChains;
    private MinecraftVersion minecraftVersion;
    private MinecraftVersion worldVersion;
    private String mixinPackage;
    private boolean debugEnabled;
    private boolean loggingEnabled;

    public MixinPluginBase(Consumer<MixinPluginCustomizer> customizeFunction) {
        if (customizeFunction != null) {
            MixinPluginCustomizer customizer = new MixinPluginCustomizer();
            customizeFunction.accept(customizer);
            initialize(customizer);
        }

        this.constraintChains = new HashMap<>();
        registerConstraints();
    }

    protected abstract void registerConstraints();

    protected void onLoad() {
        // should be implemented by child
    }

    private void initialize(MixinPluginCustomizer customizer) {
        this.loggingEnabled = customizer.loggingEnabled;
        this.debugEnabled = loggingEnabled && customizer.debugEnabled;

        if (debugEnabled) {
            log.info("[Debug] Initializing mixin plugin with customizer:");
            log.info("[Debug] {}", customizer);
        }

        if (customizer.useVersionJson) {
            if (!loadFromVersionJson()) {
                if (loggingEnabled) {
                    log.warn("World version constrained mixins will not be used!");
                }
            }
        }

        if (worldVersion == null)
            this.worldVersion = customizer.defaultWorldVersion;

        if (minecraftVersion == null) {
            String profile = customizer.runningVersion;
            if (profile != null) {
                Optional<MinecraftVersion> version = MinecraftVersion.ofProfile(profile);
                if (version.isPresent()) {
                    this.minecraftVersion = version.get();
                } else if (loggingEnabled) {
                    log.warn("Couldn't resolve Minecraft version from the profile '{}'!", profile);
                    log.warn("Minecraft version constrained mixins will not be used!");
                }
            }

            if (minecraftVersion == null) {
                this.minecraftVersion = customizer.defaultMinecraftVersion;
            }
        }

        if (debugEnabled)
            log.info("[Debug] Initialized with: { MC version = '{}', world version = '{}' }", minecraftVersion, worldVersion);

        if (minecraftVersion == null) {
            log.error("Minecraft version isn't defined: no mixins will be applied!");
        }
    }

    @Override
    public final void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;

        if (debugEnabled)
            log.info("[Debug] Using mixin package = '{}'", mixinPackage);

        onLoad();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (minecraftVersion == null)
            return false;

        for (String mixinSuffix : constraintChains.keySet()) {
            if (mixinClassName.endsWith(mixinSuffix)) {
                List<MixinConstraintChain> chains = constraintChains.get(mixinSuffix);
                if (chains == null || chains.isEmpty())
                    continue;

                return chains.stream().anyMatch(chain -> chain.passBoth(minecraftVersion, worldVersion));
            }
        }

        return true;
    }

    protected final MixinConstraintGroup createConstraintGroup(String childPackageName) {
        String mixinClassPrefix = String.format("%s.%s.Mixin", mixinPackage, childPackageName);

        if (debugEnabled)
            log.info("[Debug] Creating mixin constraint group with class prefix '{}'", mixinClassPrefix);

        return new MixinConstraintGroup() {
            private final Map<String, List<MixinConstraintChain>> constraintChains = new LinkedHashMap<>();

            @Override
            public MixinConstraintGroup add(String mixinClassName, String expression) {
                String mixinClass = mixinClassPrefix + mixinClassName;
                List<MixinConstraintChain> chains = constraintChains.computeIfAbsent(
                        mixinClass,
                        key -> new ArrayList<>()
                );

                MixinConstraintChain chain = new MixinConstraintChain();
                MixinConstraint.parse(expression, chain::append);
                chains.add(chain);

                if (debugEnabled) {
                    log.info("[Debug] Added mixin constraint chain for '{}':", mixinClass);
                    chain.forEach(constraint -> log.info("[Debug] - {}", constraint));
                }

                return this;
            }

            @Override
            public void apply() {
                MixinPluginBase.this.constraintChains.putAll(constraintChains);
            }
        };
    }

    private boolean loadFromVersionJson() {
        try (InputStream resource = ClassLoader.getSystemClassLoader().getResourceAsStream("version.json")) {
            if (resource == null) {
                log.error("ELFeatures can't find 'version.json' resource in classpath!");
                log.error("Seems that you're running MC version earlier than 18w47b or your client JAR is corrupted.");
                return false;
            }

            JsonObject root = new JsonParser().parse(new InputStreamReader(resource)).getAsJsonObject();
            String profile = root.get("name").getAsString();
            int worldVersion = root.get("world_version").getAsInt();

            if (loggingEnabled) {
                log.info("Running MC {} (world version: #{})", profile, worldVersion);
            }

            MinecraftVersion.ofProfile(profile).ifPresent(version -> this.minecraftVersion = version);
            MinecraftVersion.ofWorldVersion(worldVersion).ifPresent(version -> this.worldVersion = version);
            return true;
        } catch (Exception ex) {
            log.error("An exception occurred when reading 'version.json'!");
            log.error(ex);
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        if (debugEnabled) {
            if (myTargets.isEmpty()) {
                log.info("[Debug] No owned mixin targets accepted!");
            } else {
                log.info("[Debug] Accepting owned targets:");
                myTargets.stream().sorted().forEach(target -> log.info("[Debug] - '{}'", target));
            }
        }
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    public static final class MixinPluginCustomizer {

        private String runningVersion;
        private boolean useVersionJson;
        private MinecraftVersion defaultMinecraftVersion;
        private MinecraftVersion defaultWorldVersion;
        private boolean debugEnabled = ELFeaturesMod.DEBUG_ENABLED;
        private boolean loggingEnabled = ELFeaturesMod.LOGGING_ENABLED;

        public MixinPluginCustomizer useMinecraftVersion(MinecraftVersion version) {
            if (version == null || version.getType().isWorldVersion())
                throw new IllegalArgumentException("Invalid Minecraft version: " + version);

            this.defaultMinecraftVersion = version;
            return this;
        }

        public MixinPluginCustomizer useRunningVersion(String version) {
            this.runningVersion = version;
            return this;
        }

        public MixinPluginCustomizer useVersionJson() {
            this.useVersionJson = true;
            return this;
        }

        public MixinPluginCustomizer useWorldVersion(MinecraftVersion version) {
            if (version == null || !version.getType().isWorldVersion())
                throw new IllegalArgumentException("Invalid world version: " + version);

            this.defaultWorldVersion = version;
            return this;
        }

        public MixinPluginCustomizer withDebug(boolean enabled) {
            this.debugEnabled = enabled;
            return this;
        }

        public MixinPluginCustomizer withLogging(boolean enabled) {
            this.loggingEnabled = enabled;
            return this;
        }

        @Override
        public String toString() {
            return "MixinPluginCustomizer{" +
                    "runningVersion='" + runningVersion + '\'' +
                    ", useVersionJson=" + useVersionJson +
                    ", defaultMinecraftVersion=" + defaultMinecraftVersion +
                    ", defaultWorldVersion=" + defaultWorldVersion +
                    ", debugEnabled=" + debugEnabled +
                    ", loggingEnabled=" + loggingEnabled +
                    '}';
        }

    }

}

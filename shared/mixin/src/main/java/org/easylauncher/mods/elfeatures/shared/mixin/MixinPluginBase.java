package org.easylauncher.mods.elfeatures.shared.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.version.MinecraftVersion;
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

    public MixinPluginBase(Consumer<MixinPluginCustomizer> customizeFunction) {
        if (customizeFunction != null) {
            MixinPluginCustomizer customizer = new MixinPluginCustomizer();
            customizeFunction.accept(customizer);
            initialize(customizer);
        }

        this.constraintChains = new HashMap<>();
    }

    private void initialize(MixinPluginCustomizer customizer) {
        if (customizer.useCurrentWorldVersion) {
            if (!resolveCurrentWorldVersion()) {
                log.warn("World version constrained mixins will not be used!");
            }
        }

        if (worldVersion == null)
            this.worldVersion = customizer.defaultWorldVersion;

        if (minecraftVersion == null) {
            String profile = customizer.profile;
            if (profile != null) {
                Optional<MinecraftVersion> version = MinecraftVersion.ofProfile(profile);
                if (version.isPresent()) {
                    this.minecraftVersion = version.get();
                } else {
                    log.warn("Couldn't resolve Minecraft version from the profile '{}'!", profile);
                    log.warn("Minecraft version constrained mixins will not be used!");
                }
            }

            if (minecraftVersion == null) {
                this.minecraftVersion = customizer.defaultMinecraftVersion;
            }
        }

        if (minecraftVersion == null) {
            log.error("Minecraft version isn't defined: no mixins will be applied!");
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (minecraftVersion == null)
            return true;

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
        String mixinClassPrefix = String.format(".%s.Mixin", childPackageName);
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
                return this;
            }

            @Override
            public void apply() {
                MixinPluginBase.this.constraintChains.putAll(constraintChains);
            }
        };
    }

    private boolean resolveCurrentWorldVersion() {
        try (InputStream resource = ClassLoader.getSystemClassLoader().getResourceAsStream("version.json")) {
            if (resource == null) {
                log.error("ELFeatures can't find 'version.json' resource in classpath!");
                log.error("Seems that you're running MC version earlier than 18w47b or your client JAR is corrupted.");
                return false;
            }

            JsonObject root = new JsonParser().parse(new InputStreamReader(resource)).getAsJsonObject();
            String profile = root.get("name").getAsString();
            int worldVersion = root.get("world_version").getAsInt();

            log.info("Running MC {} (world version: #{})", profile, worldVersion);
            MinecraftVersion.ofProfile(profile).ifPresent(version -> this.minecraftVersion = version);
            MinecraftVersion.ofWorldVersion(worldVersion).ifPresent(version -> this.worldVersion = version);
            return true;
        } catch (Exception ex) {
            log.error("An exception occurred when reading 'version.json'!");
            log.error(ex);
            return false;
        }
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    public static final class MixinPluginCustomizer {

        private String profile;
        private boolean useCurrentWorldVersion;
        private MinecraftVersion defaultMinecraftVersion;
        private MinecraftVersion defaultWorldVersion;

        public MixinPluginCustomizer useCurrentMinecraftVersion(String profile) {
            this.profile = profile;
            return this;
        }

        public MixinPluginCustomizer useMinecraftVersion(MinecraftVersion version) {
            if (version == null || version.getType().isWorldVersion())
                throw new IllegalArgumentException("Invalid Minecraft version: " + version);

            this.defaultMinecraftVersion = version;
            return this;
        }

        public MixinPluginCustomizer useCurrentWorldVersion() {
            this.useCurrentWorldVersion = true;
            return this;
        }

        public MixinPluginCustomizer useWorldVersion(MinecraftVersion version) {
            if (version == null || !version.getType().isWorldVersion())
                throw new IllegalArgumentException("Invalid world version: " + version);

            this.defaultWorldVersion = version;
            return this;
        }

    }

}

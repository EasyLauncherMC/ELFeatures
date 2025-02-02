package org.easylauncher.mods.elfeatures.shared.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Log4j2
public abstract class MixinPluginBase implements IMixinConfigPlugin {

    private final Map<String, MixinConstraint> constraints;
    private int dataVersion;

    public MixinPluginBase() {
        this.constraints = new HashMap<>();
    }

    @Override
    public void onLoad(String mixinPackage) {
        try (InputStream resource = ClassLoader.getSystemClassLoader().getResourceAsStream("version.json")) {
            if (resource == null) {
                log.warn("ELFeatures can't find 'version.json' resource!");
                log.warn("Seems that you're running MC version earlier than 18w47b or your client JAR is corrupted.");
                return;
            }

            JsonObject root = new JsonParser().parse(new InputStreamReader(resource)).getAsJsonObject();
            String name = root.get("name").getAsString();
            this.dataVersion = root.get("world_version").getAsInt();

            log.info("Running MC {} (data version: #{})", name, dataVersion);
        } catch (Exception ex) {
            log.error("An exception occurred when reading 'version.json'!");
            log.error(ex);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (dataVersion == 0)
            return false;

        for (String mixinSuffix : constraints.keySet()) {
            if (mixinClassName.endsWith(mixinSuffix)) {
                MixinConstraint constraint = constraints.get(mixinSuffix);
                return constraint == null || constraint.pass(dataVersion);
            }
        }

        return true;
    }

    protected final MixinConstraintGroup createConstraintGroup(String childPackageName) {
        String mixinClassPrefix = String.format(".%s.Mixin", childPackageName);
        return new MixinConstraintGroup() {
            private final Map<String, MixinConstraint> constraints = new LinkedHashMap<>();

            @Override
            public MixinConstraintGroup add(String mixinClassName, String expression) {
                this.constraints.put(mixinClassPrefix + mixinClassName, MixinConstraint.parse(expression));
                return this;
            }

            @Override
            public void apply() {
                MixinPluginBase.this.constraints.putAll(constraints);
            }
        };
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}

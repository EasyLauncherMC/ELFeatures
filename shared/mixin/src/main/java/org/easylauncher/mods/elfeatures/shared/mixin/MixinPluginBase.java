package org.easylauncher.mods.elfeatures.shared.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.CustomLog;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

@CustomLog
public abstract class MixinPluginBase implements IMixinConfigPlugin {

    private final Map<String, MixinConstraint> constraints;
    private int dataVersion;

    public MixinPluginBase() {
        this.constraints = new HashMap<>();
    }

    @Override
    public void onLoad(String mixinPackage) {
        try (InputStream resource = openVersionJson()) {
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
            log.error("An exception occurred when reading 'version.json'!", ex);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        MixinConstraint constraint = null;
        for (String mixinSuffix : constraints.keySet()) {
            if (mixinClassName.endsWith(mixinSuffix)) {
                constraint = constraints.get(mixinSuffix);
                break;
            }
        }

        if (constraint == null)
            return true;

        return dataVersion != 0 && constraint.pass(dataVersion);
    }

    // Forge 1.17+ keeps the client jar off the system class loader (ModLauncher union / modules)
    private static InputStream openVersionJson() {
        InputStream in;

        try {
            in = MixinService.getService().getResourceAsStream("version.json");
            if (in != null) return in;
        } catch (Throwable ignored) {
        }

        ClassLoader[] loaders = {
                Thread.currentThread().getContextClassLoader(),
                MixinPluginBase.class.getClassLoader(),
                ClassLoader.getSystemClassLoader(),
        };

        for (ClassLoader loader : loaders) {
            if (loader == null) continue;
            in = loader.getResourceAsStream("version.json");
            if (in != null) return in;
        }

        in = openVersionJsonBeside("net.minecraft.DetectedVersion");
        if (in != null) return in;

        return openVersionJsonBeside("net.minecraft.client.Minecraft");
    }

    private static InputStream openVersionJsonBeside(String className) {
        try {
            Class<?> type = MixinService.getService().getClassProvider().findClass(className, false);
            InputStream in = type.getResourceAsStream("/version.json");
            if (in != null) return in;

            if (type.getClassLoader() != null) {
                in = type.getClassLoader().getResourceAsStream("version.json");
                if (in != null) return in;
            }

            if (type.getProtectionDomain() == null || type.getProtectionDomain().getCodeSource() == null)
                return null;

            URL location = type.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) return null;

            try {
                return new URL(location, "version.json").openStream();
            } catch (Exception ignored) {
            }

            return openVersionJsonInJar(location);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static InputStream openVersionJsonInJar(URL location) {
        String spec = location.toExternalForm();
        int jar = spec.toLowerCase(Locale.ROOT).indexOf(".jar");
        if (jar < 0) return null;

        String jarUrl = spec.substring(0, jar + 4);
        if (jarUrl.startsWith("union:"))
            jarUrl = "file:" + jarUrl.substring("union:".length());

        try {
            return new URL("jar:" + jarUrl + "!/version.json").openStream();
        } catch (Exception ignored) {
            return null;
        }
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

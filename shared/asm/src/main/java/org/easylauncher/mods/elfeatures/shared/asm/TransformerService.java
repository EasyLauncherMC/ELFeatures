package org.easylauncher.mods.elfeatures.shared.asm;

import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.ClassTransformer;
import org.easylauncher.mods.elfeatures.shared.asm.transformer.MethodTransformer;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public final class TransformerService {

    public static final boolean DEBUG_ENABLED = "true".equalsIgnoreCase(System.getProperty("elfeatures.debug.enabled"));

    private final Remapper remapper;
    private final Map<String, ClassTransformer> classMap;
    private final Map<String, Map<String, MethodTransformer>> methodSrgMap;
    private final Map<String, Map<String, MethodTransformer>> methodMap;

    public TransformerService(
            Remapper remapper,
            ClassTransformer.Instantiator[] classTransformers,
            MethodTransformer.Instantiator[] methodTransformers
    ) {
        this.remapper = remapper;
        this.classMap = new HashMap<>();
        this.methodSrgMap = new HashMap<>();
        this.methodMap = new HashMap<>();

        for (ClassTransformer.Instantiator instantiator : classTransformers) {
            ClassTransformer transformer = instantiator.createInstance(this);
            TransformerTarget target = getTransformTarget(transformer.getClass());
            if (target != null) {
                registerClassTransformer(target, transformer);
            }
        }

        for (MethodTransformer.Instantiator instantiator : methodTransformers) {
            MethodTransformer transformer = instantiator.createInstance(this);
            TransformerTarget target = getTransformTarget(transformer.getClass());
            if (target != null) {
                registerMethodTransformer(target, transformer);
            }
        }
    }

    public ClassNode transformClass(ClassNode node, String name) {
        ClassTransformer transformer = classMap.get(name);
        if (transformer == null)
            return node;

        String transformerName = getTransformerName(transformer.getClass());
        try {
            node = transformer.transform(node);
            if (DEBUG_ENABLED) {
                log.debug("Transformed class: '{}' (with '{}')", name, transformerName);
            }
        } catch (Exception ex) {
            log.error("Failed to transform class: '{}' (with '{}')", name, transformerName);
            log.error(ex);
        }

        return node;
    }

    public MethodNode transformMethod(ClassNode classNode, String className, MethodNode node, String name, String desc) {
        String methodTarget = name + ' ' + desc;
        MethodTransformer transformer = null;

        Map<String, MethodTransformer> srgMap = methodSrgMap.get(className);
        if (srgMap != null)
            transformer = srgMap.get(methodTarget);

        Map<String, MethodTransformer> map = methodMap.get(className);
        if (transformer == null && map != null)
            transformer = map.get(methodTarget);

        if (transformer == null) {
            boolean potentialMismatch = false;
            String prefix = name + ' ';

            if (srgMap != null)
                potentialMismatch = srgMap.keySet().stream().anyMatch(key -> key.startsWith(prefix));

            if (!potentialMismatch && map != null)
                potentialMismatch = map.keySet().stream().anyMatch(key -> key.startsWith(prefix));

            if (potentialMismatch) {
                log.error("Detected potential method mismatch!");
                log.error("Requested target: '{}'", methodTarget);
                log.error("Known SRG methods: {}", srgMap != null ? srgMap : "<undefined>");
                log.error("Known methods: {}", map != null ? map : "<undefined>");
            } else {
                boolean hasSameDesc = false;
                String suffix = ' ' + desc;

                if (srgMap != null)
                    hasSameDesc = srgMap.keySet().stream().anyMatch(key -> key.endsWith(suffix));

                if (!hasSameDesc && map != null)
                    hasSameDesc = map.keySet().stream().anyMatch(key -> key.endsWith(suffix));

                if (hasSameDesc) {
                    log.warn("Found potentially correct method '{}'!", methodTarget);
                } else {
                    log.debug("Skipped method '{}'", methodTarget);
                }
            }

            return node;
        }

        String transformerName = getTransformerName(transformer.getClass());
        try {
            node = transformer.transform(classNode, node);
            if (DEBUG_ENABLED) {
                log.debug("Transformed method: '{}' in class '{}' (with '{}')", name, className, transformerName);
            }
        } catch (Exception ex) {
            log.error("Failed to transform method: '{}' in class '{}' (with '{}')", name, className, transformerName);
            log.error(ex);
        }

        return node;
    }

    public boolean checkClassName(String deobfName, String name) {
        return deobfName.equals(remapper.mapType(name));
    }

    public boolean checkMethod(String srgName, String owner, String name, String desc) {
        return srgName.equals(remapper.mapMethodName(owner, name, desc));
    }

    public boolean checkMethodDesc(String deobfDesc, String desc)  {
        return deobfDesc.equals(remapper.mapMethodDesc(desc));
    }

    public boolean checkField(String srgName, String owner, String name, String desc) {
        return srgName.equals(remapper.mapFieldName(owner, name, desc));
    }

    public boolean checkFieldDesc(String deobfDesc, String desc)  {
        return deobfDesc.equals(remapper.mapDesc(desc));
    }

    public boolean containsName(String name) {
        if (name == null || name.isEmpty())
            return false;

        return classMap.containsKey(name) || methodSrgMap.containsKey(name) || methodMap.containsKey(name);
    }

    private void registerClassTransformer(TransformerTarget target, ClassTransformer transformer) {
        if (classMap.containsKey(target.className()))
            return;

        classMap.put(target.className(), transformer);
        if (DEBUG_ENABLED) {
            log.debug("Registered transformer for class '{}'", target.className());
        }
    }

    private void registerMethodTransformer(TransformerTarget target, MethodTransformer transformer) {
        if (!target.methodNameSrg().isEmpty()) {
            String methodNameDesc = target.methodNameSrg() + ' ' + target.methodDesc();
            methodSrgMap.computeIfAbsent(target.className(), k -> new HashMap<>()).put(methodNameDesc, transformer);
            if (DEBUG_ENABLED) {
                log.debug(
                        "Registered transformer for SRG method '{}' in class '{}'",
                        methodNameDesc,
                        target.className()
                );
            }
        }

        for (String methodName : target.methodNames()) {
            String methodNameDesc = methodName + ' ' + target.methodDesc();
            methodMap.computeIfAbsent(target.className(), k -> new HashMap<>()).put(methodNameDesc, transformer);
            if (DEBUG_ENABLED) {
                log.debug(
                        "Registered transformer for method '{}' in class '{}'",
                        methodNameDesc,
                        target.className()
                );
            }
        }
    }

    private TransformerTarget getTransformTarget(Class<?> clazz) {
        TransformerTarget annotation = clazz.getAnnotation(TransformerTarget.class);
        if (annotation == null)
            throw new IllegalArgumentException(String.format("Transformer '%s' is not annotated with ASMTarget!", clazz.getName()));

        return annotation;
    }

    private String getTransformerName(Class<?> transformerType) {
        StringBuilder name = new StringBuilder(transformerType.getSimpleName());

        Class<?> declaringClass = transformerType.getDeclaringClass();
        while (declaringClass != null) {
            name.insert(0, '.');
            name.insert(0, declaringClass.getSimpleName());
            declaringClass = declaringClass.getDeclaringClass();
        }

        return name.toString();
    }

}

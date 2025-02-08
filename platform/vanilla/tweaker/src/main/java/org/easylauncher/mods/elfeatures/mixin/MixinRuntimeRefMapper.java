package org.easylauncher.mods.elfeatures.mixin;

import org.objectweb.asm.Type;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.mixin.refmap.IClassReferenceMapper;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Quantifier;

public final class MixinRuntimeRefMapper implements IClassReferenceMapper, IReferenceMapper {

    private static final ILogger log = MixinService.getService().getLogger("mixin");

    private final IReferenceMapper refMap;
    private final IRemapper remapper;

    public MixinRuntimeRefMapper(MixinEnvironment environment, IReferenceMapper refMap) {
        this.refMap = refMap;
        this.remapper = environment.getRemappers();
        log.debug("Remapping refMap {} using remapper chain", refMap.getResourceName());
    }

    @Override
    public boolean isDefault() {
        return refMap.isDefault();
    }

    @Override
    public String getResourceName() {
        return refMap.getResourceName();
    }

    @Override
    public String getStatus() {
        return refMap.getStatus();
    }

    @Override
    public String getContext() {
        return refMap.getContext();
    }

    @Override
    public void setContext(String context) {
        refMap.setContext(context);
    }

    @Override
    public String remapClassName(String mixinClassName, String reference) {
        return remapClassNameWithContext(getContext(), mixinClassName, reference);
    }

    @Override
    public String remapClassNameWithContext(String context, String mixinClassName, String reference) {
        if (reference == null || reference.isEmpty())
            return reference;

        log.debug("#remapClassNameWithContext('{}', '{}', '{}')", context, mixinClassName, reference);

        String remappedReference = refMap.remapWithContext(context, mixinClassName, reference);
        if (remappedReference == null || remappedReference.isEmpty())
            remappedReference = reference;

        String mappedClassName = remapper.map(remappedReference);
        if (mappedClassName == null || mappedClassName.isEmpty())
            mappedClassName = remappedReference;

        return mappedClassName;
    }

    @Override
    public String remap(String mixinClassName, String reference) {
        return remapWithContext(getContext(), mixinClassName, reference);
    }

    @Override
    public String remapWithContext(String context, String mixinClassName, String reference) {
        if (reference == null || reference.isEmpty())
            return reference;

        log.debug("#remapWithContext('{}', '{}', '{}')", context, mixinClassName, reference);

        String remappedReference = refMap.remapWithContext(context, mixinClassName, reference);
        if (remappedReference == null || remappedReference.isEmpty())
            remappedReference = reference;

        MemberInfo info = MemberInfo.parse(remappedReference, null);
        String owner = info.getOwner();
        String name = info.getName();
        String desc = info.getDesc();

        // class
        if (name == null && desc == null) {
            if (owner == null)
                return info.toString();

            String mappedOwner = remapper.map(owner);
            return new MemberInfo(mappedOwner, Quantifier.DEFAULT).toString();
        }

        String mappedOwner = owner != null ? remapper.map(owner) : null;
        String mappedDesc = desc != null ? remapper.mapDesc(desc) : null;

        if (info.isField()) {
            // field
            String mappedName = remapper.mapFieldName(owner, name, desc);
            return new MemberInfo(mappedName, mappedOwner, mappedDesc).toString();
        } else {
            // method
            String mappedName = remapper.mapMethodName(owner, name, desc);
            return new MemberInfo(mappedName, mappedOwner, mappedDesc).toString();
        }
    }

    private static String remapMethodDescriptor(IRemapper remapper, String desc) {
        StringBuilder newDesc = new StringBuilder("(");

        for (Type arg : Type.getArgumentTypes(desc))
            newDesc.append(remapper.mapDesc(arg.getDescriptor()));

        newDesc.append(')');
        newDesc.append(remapper.mapDesc(Type.getReturnType(desc).getDescriptor()));

        return newDesc.toString();
    }

}

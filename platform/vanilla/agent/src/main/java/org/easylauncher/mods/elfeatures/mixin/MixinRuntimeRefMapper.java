package org.easylauncher.mods.elfeatures.mixin;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.mixin.refmap.IClassReferenceMapper;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Quantifier;

/**
 * The refmap the mixins were built with, put through the remapper chain on the way out.
 *
 * <p>The refmap names the game in intermediary, which is one step short of what the running release calls it.
 * Wrapping it here is what {@code refmapWrapper} in the mixin configuration is for.
 */
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

            return new MemberInfo(remapper.map(owner), Quantifier.DEFAULT).toString();
        }

        String mappedOwner = owner != null ? remapper.map(owner) : null;
        String mappedDesc = desc != null ? remapper.mapDesc(desc) : null;

        // a descriptor with no member to it
        // that is what @At("NEW") names, and there is nothing to look up
        String mappedName = name == null ? null
                : info.isField() ? remapper.mapFieldName(owner, name, desc)
                : remapper.mapMethodName(owner, name, desc);

        return new MemberInfo(mappedName, mappedOwner, mappedDesc).toString();
    }

}

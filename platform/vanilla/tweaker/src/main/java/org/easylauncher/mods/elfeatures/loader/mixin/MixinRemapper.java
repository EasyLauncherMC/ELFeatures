package org.easylauncher.mods.elfeatures.loader.mixin;

import lombok.AllArgsConstructor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

@AllArgsConstructor
public final class MixinRemapper implements IRemapper {

    private final MappingTree mappings;
    private final int fromId;
    private final int toId;

    public MixinRemapper(MappingTree mappings, String from, String to) {
        this(mappings, mappings.getNamespaceId(from), mappings.getNamespaceId(to));
    }

    @Override
    public String map(String typeName) {
        return mappings.mapClassName(typeName, fromId, toId);
    }

    @Override
    public String mapDesc(String desc) {
        return mappings.mapDesc(desc, fromId, toId);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        FieldMapping field = mappings.getField(owner, name, desc, fromId);
        if (field != null)
            return field.getName(toId);

        field = mappings.getField(unmap(owner), name, mappings.mapDesc(desc, toId, fromId), fromId);
        return field != null ? field.getName(toId) : name;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        MethodMapping method = mappings.getMethod(owner, name, desc, fromId);
        if (method != null)
            return method.getName(toId);

        method = mappings.getMethod(unmap(owner), name, mappings.mapDesc(desc, toId, fromId), fromId);
        return method != null ? method.getName(toId) : name;
    }

    @Override
    public String unmap(String typeName) {
        return mappings.mapClassName(typeName, toId, fromId);
    }

    @Override
    public String unmapDesc(String desc) {
        return mappings.mapDesc(desc, toId, fromId);
    }

}

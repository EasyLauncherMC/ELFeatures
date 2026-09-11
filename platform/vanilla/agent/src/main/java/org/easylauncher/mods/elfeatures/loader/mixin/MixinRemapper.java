package org.easylauncher.mods.elfeatures.loader.mixin;

import lombok.AllArgsConstructor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MappingTree.MemberMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public final class MixinRemapper implements IRemapper {

    private final MappingTree mappings;
    private final int fromId;
    private final int toId;

    /** every member name in the source namespace, for the members a mixin names on a class that inherits them */
    private Map<String, String> memberNames;

    public MixinRemapper(MappingTree mappings, String from, String to) {
        this(mappings, mappings.getNamespaceId(from), mappings.getNamespaceId(to), null);
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
        if (owner == null || desc == null)
            return inheritedNameFrom(name);

        FieldMapping field = mappings.getField(owner, name, desc, fromId);
        if (field != null)
            return field.getName(toId);

        field = mappings.getField(unmap(owner), name, mappings.mapDesc(desc, toId, fromId), fromId);
        return field != null ? field.getName(toId) : inheritedNameFrom(name);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        if (owner == null || desc == null)
            return inheritedNameFrom(name);

        MethodMapping method = mappings.getMethod(owner, name, desc, fromId);
        if (method != null)
            return method.getName(toId);

        method = mappings.getMethod(unmap(owner), name, mappings.mapDesc(desc, toId, fromId), fromId);
        return method != null ? method.getName(toId) : inheritedNameFrom(name);
    }

    @Override
    public String unmap(String typeName) {
        return mappings.mapClassName(typeName, toId, fromId);
    }

    @Override
    public String unmapDesc(String desc) {
        return mappings.mapDesc(desc, toId, fromId);
    }

    private String inheritedNameFrom(String name) {
        if (memberNames == null)
            this.memberNames = indexMemberNames();

        String mapped = memberNames.get(name);
        return mapped != null ? mapped : name;
    }

    private Map<String, String> indexMemberNames() {
        Map<String, String> names = new HashMap<String, String>();

        for (ClassMapping type : mappings.getClasses()) {
            for (MemberMapping method : type.getMethods())
                putName(names, method);

            for (MemberMapping field : type.getFields()) {
                putName(names, field);
            }
        }

        return names;
    }

    private void putName(Map<String, String> names, MemberMapping member) {
        String from = member.getName(fromId);
        String to = member.getName(toId);

        if (from != null && to != null) {
            names.put(from, to);
        }
    }

}

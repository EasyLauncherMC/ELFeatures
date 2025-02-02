package org.easylauncher.mods.elfeatures.shared.asm.transformer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.easylauncher.mods.elfeatures.shared.asm.TransformerService;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

@Log4j2
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class BaseTransformer {

    protected final TransformerService transformerService;

    protected final boolean checkOpcodes(InsnList instructions, int start, int... opcodes) {
        if (instructions.size() <= start + opcodes.length)
            return false;

        if (start < 0 || start + opcodes.length < 0)
            return false;

        for (int i = 0; i < opcodes.length; i++)
            if (instructions.get(start + i).getOpcode() != opcodes[i])
                return false;

        return true;
    }

    protected final boolean checkMethod(AbstractInsnNode insn, int opcode, String method) {
        return insn.getOpcode() == opcode && checkMethod(insn, method);
    }

    protected final boolean checkMethod(AbstractInsnNode insn, String method) {
        int dotIndex = method.indexOf('.');
        int spaceIndex = method.indexOf(' ');

        if (dotIndex == -1 || spaceIndex == -1)
            throw new IllegalArgumentException("Incorrect syntax for method parameter!");

        if (insn instanceof MethodInsnNode) {
            String ownerName = method.substring(0, dotIndex);
            String methodName = method.substring(dotIndex + 1, spaceIndex);
            String methodDesc = method.substring(spaceIndex + 1);

            MethodInsnNode minsn = (MethodInsnNode) insn;
            return checkClassName(ownerName, minsn) && checkMethodName(methodName, minsn) && checkMethodDesc(methodDesc, minsn);
        }

        return false;
    }

    protected final boolean checkField(AbstractInsnNode insn, int opcode, String field) {
        return insn.getOpcode() == opcode && checkField(insn, field);
    }

    protected final boolean checkField(AbstractInsnNode insn, String field) {
        int dotIndex = field.indexOf('.');
        int colonIndex = field.indexOf(':');

        if (dotIndex == -1 || colonIndex == -1)
            throw new IllegalArgumentException("Incorrect syntax for field parameter!");

        if (insn instanceof FieldInsnNode) {
            String ownerName = field.substring(0, dotIndex);
            String fieldName = field.substring(dotIndex + 1, colonIndex - 1);
            String fieldDesc = field.substring(colonIndex + 2);

            FieldInsnNode finsn = (FieldInsnNode) insn;
            return checkClassName(ownerName, finsn) && checkFieldName(fieldName, finsn) && checkFieldDesc(fieldDesc, finsn);
        }

        return false;
    }

    protected final boolean checkClassName(String name, MethodInsnNode insn) {
        return checkClassName(name, insn.owner);
    }

    protected final boolean checkClassName(String name, FieldInsnNode insn) {
        return checkClassName(name, insn.owner);
    }

    protected final boolean checkClassName(String name, String obfName) {
        return transformerService.checkClassName(name, obfName);
    }

    protected final boolean checkMethodName(String name, MethodInsnNode insn) {
        return checkMethodName(name, insn.owner, insn.name, insn.desc);
    }

    protected final boolean checkMethodName(String name, String owner, String obfName, String desc) {
        return transformerService.checkMethod(name, owner, obfName, desc);
    }

    protected final boolean checkMethodDesc(String desc, MethodInsnNode insn) {
        return checkMethodDesc(desc, insn.desc);
    }

    protected final boolean checkMethodDesc(String desc, String obfDesc) {
        return transformerService.checkMethodDesc(desc, obfDesc);
    }

    protected final boolean checkFieldName(String name, AbstractInsnNode insn) {
        return insn instanceof FieldInsnNode && checkFieldName(name, (FieldInsnNode) insn);
    }

    protected final boolean checkFieldName(String name, FieldInsnNode insn) {
        return checkFieldName(name, insn.owner, insn.name, insn.desc);
    }

    protected final boolean checkFieldName(String name, String owner, String obfName, String desc) {
        return transformerService.checkField(name, owner, obfName, desc);
    }

    protected final boolean checkFieldDesc(String desc, AbstractInsnNode insn) {
        return insn instanceof FieldInsnNode && checkFieldDesc(desc, (FieldInsnNode) insn);
    }

    protected final boolean checkFieldDesc(String desc, FieldInsnNode insn) {
        return checkFieldDesc(desc, insn.desc);
    }

    protected final boolean checkFieldDesc(String desc, String obfDesc) {
        return transformerService.checkFieldDesc(desc, obfDesc);
    }

}

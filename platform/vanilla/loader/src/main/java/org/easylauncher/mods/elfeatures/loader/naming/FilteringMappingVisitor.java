package org.easylauncher.mods.elfeatures.loader.naming;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

/** Keeps only what a remapper reads: classes, fields and methods, none of the local names or comments. */
public final class FilteringMappingVisitor extends ForwardingMappingVisitor {

    public FilteringMappingVisitor(MappingVisitor next) {
        super(next);
    }

    @Override
    public boolean visitMethodArg(int argPosition, int lvIndex, String srcName) {
        return false;
    }

    @Override
    public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, String srcName) {
        return false;
    }

    @Override
    public void visitComment(MappedElementKind targetKind, String comment) {
        // nothing to do here
    }

}

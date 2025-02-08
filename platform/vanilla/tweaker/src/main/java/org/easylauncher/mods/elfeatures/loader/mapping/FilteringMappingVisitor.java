package org.easylauncher.mods.elfeatures.loader.mapping;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

public class FilteringMappingVisitor extends ForwardingMappingVisitor {

	public FilteringMappingVisitor(MappingVisitor next) {
		super(next);
	}

	@Override
	public boolean visitMethodArg(int argPosition, int lvIndex, String srcName) {
		// ignored
		return false;
	}

	@Override
	public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, String srcName) {
		// ignored
		return false;
	}

	@Override
	public void visitComment(MappedElementKind targetKind, String comment) {
		// ignored
	}

}
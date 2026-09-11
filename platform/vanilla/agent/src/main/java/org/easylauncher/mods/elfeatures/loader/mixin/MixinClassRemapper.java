package org.easylauncher.mods.elfeatures.loader.mixin;

import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

/**
 * The mapping tree as ASM takes it, for rewriting a mixin class into the names the running game has.
 *
 * <p>Mixin itself only puts annotation references through its remapper chain. The mixin class's own bytecode —
 * a handler's signature, a call to the game from a handler's body — it leaves alone, because every loader it
 * was written for runs a game named the same way the mixins are.
 */
final class MixinClassRemapper extends Remapper {

    MixinClassRemapper() {
        super(Opcodes.ASM9);
    }

    @Override
    public String map(String internalName) {
        IRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        if (remapper == null)
            return internalName;

        String mapped = remapper.map(internalName);
        return mapped != null ? mapped : internalName;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        IRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        return remapper != null ? remapper.mapFieldName(owner, name, descriptor) : name;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        IRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        return remapper != null ? remapper.mapMethodName(owner, name, descriptor) : name;
    }

}

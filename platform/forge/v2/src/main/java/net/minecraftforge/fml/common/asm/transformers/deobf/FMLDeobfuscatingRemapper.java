package net.minecraftforge.fml.common.asm.transformers.deobf;

import org.objectweb.asm.commons.Remapper;

public class FMLDeobfuscatingRemapper extends Remapper {

    public static final FMLDeobfuscatingRemapper INSTANCE = new FMLDeobfuscatingRemapper();

}
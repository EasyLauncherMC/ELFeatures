package net.minecraftforge.fml.loading.moddiscovery;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractModProvider implements IModLocator {

    protected IModLocator.ModFileOrException createMod(Path path) {
        return null;
    }

    @Override
    public boolean isValid(IModFile modFile) {
        return false;
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {}

    @Override
    public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {}

}

package net.minecraftforge.fml.loading.moddiscovery;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public abstract class AbstractJarFileLocator implements IModLocator {

    @Override
    public void scanFile(IModFile file, Consumer<Path> pathConsumer) {}

    @Override
    public List scanMods() {
        return null;
    }

    public abstract Stream<Path> scanCandidates();

    @Override
    public boolean isValid(IModFile modFile) {
        return false;
    }

}

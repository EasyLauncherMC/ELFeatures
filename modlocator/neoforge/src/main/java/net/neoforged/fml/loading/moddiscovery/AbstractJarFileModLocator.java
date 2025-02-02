package net.neoforged.fml.loading.moddiscovery;

import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModLocator;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes"})
public abstract class AbstractJarFileModLocator implements IModLocator {

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

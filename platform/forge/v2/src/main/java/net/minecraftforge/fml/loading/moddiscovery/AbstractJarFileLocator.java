package net.minecraftforge.fml.loading.moddiscovery;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public abstract class AbstractJarFileLocator implements IModLocator {

    public Path findPath(ModFile modFile, String... path) {
        return null;
    }

    public void scanFile(ModFile file, Consumer<Path> pathConsumer) {}

    public Optional<Manifest> findManifest(Path file) {
        return Optional.empty();
    }

    public boolean isValid(ModFile modFile) {
        return false;
    }

}

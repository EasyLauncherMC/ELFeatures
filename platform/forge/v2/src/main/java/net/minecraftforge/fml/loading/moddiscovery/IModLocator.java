package net.minecraftforge.fml.loading.moddiscovery;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public interface IModLocator {

    List<ModFile> scanMods();

    String name();

    Path findPath(ModFile var1, String... var2);

    void scanFile(ModFile var1, Consumer<Path> var2);

    Optional<Manifest> findManifest(Path var1);

    void initArguments(Map<String, ?> var1);

    boolean isValid(ModFile var1);

}

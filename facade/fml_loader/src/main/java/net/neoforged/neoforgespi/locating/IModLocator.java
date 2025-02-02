package net.neoforged.neoforgespi.locating;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public interface IModLocator {

    record ModFileOrException(IModFile file, ModFileLoadingException ex) {}

    List scanMods();

    String name();

    void scanFile(IModFile var1, Consumer<Path> var2);

    void initArguments(Map<String, ?> var1);

    boolean isValid(IModFile var1);

}

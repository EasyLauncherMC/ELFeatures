package net.minecraftforge.forgespi.locating;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public interface IModLocator {

    record ModFileOrException(IModFile file, ModFileLoadingException ex) {}

    List scanMods();

    String name();

    void scanFile(IModFile modFile, Consumer<Path> pathConsumer);

    void initArguments(Map<String, ?> arguments);

    boolean isValid(IModFile modFile);

}

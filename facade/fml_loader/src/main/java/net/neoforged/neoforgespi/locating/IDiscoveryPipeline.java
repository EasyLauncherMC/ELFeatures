package net.neoforged.neoforgespi.locating;

import java.nio.file.Path;
import java.util.Optional;

public interface IDiscoveryPipeline {

    @SuppressWarnings("UnusedReturnValue")
    default Optional<IModFile> addPath(Path path, ModFileDiscoveryAttributes attributes, IncompatibleFileReporting incompatibleFileReporting) {
        return Optional.empty();
    }

}

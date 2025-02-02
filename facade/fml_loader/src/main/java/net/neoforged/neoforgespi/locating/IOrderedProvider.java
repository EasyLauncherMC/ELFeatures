package net.neoforged.neoforgespi.locating;

public interface IOrderedProvider {

    int DEFAULT_PRIORITY = 0;
    int HIGHEST_SYSTEM_PRIORITY = 1000;
    int LOWEST_SYSTEM_PRIORITY = -1000;

    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

}

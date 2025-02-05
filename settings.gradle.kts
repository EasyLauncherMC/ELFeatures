pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ELFeatures"

// --- project modules

// platform-independent logic
include(":core")

// modlocator implementations for Forge v3/v4 and NeoForge platforms
include(":modlocator:forge", ":modlocator:neoforge")

// platform modules
include(":platform:fabric")                 // Fabric [1.14,)
include(":platform:forge:transformers")     // Forge ASM transformers
include(":platform:forge:v1")               // Forge [1.7.10]
include(":platform:forge:v2")               // Forge [1.8, 1.16.5]
include(":platform:forge:v3")               // Forge [1.17, 1.20.4]
include(":platform:forge:v4")               // Forge [1.20.6,)
include(":platform:neoforge")               // NeoForge [1.20.2,)
include(":platform:vanilla:legacy")         // Vanilla [1.7.2, 1.14.4)

// platform cross-module shared code
include(":shared:asm", ":shared:mixin")

// cross-version facades for some libraries
include(":facade:authlib", ":facade:fml_loader", ":facade:fml_spi", ":facade:forgespi", ":facade:neoforgespi")

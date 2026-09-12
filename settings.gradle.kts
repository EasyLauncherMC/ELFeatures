pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases/")
        maven("https://maven.ornithemc.net/snapshots/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ELFeatures"

// --- project modules

// platform-independent logic
include(":core")

// platform modules — Fabric
include(":platform:fabric:v1")              // Fabric [1.14, 1.21.11]
include(":platform:fabric:v2")              // Fabric [26.1,)

// platform modules — Forge
include(":platform:forge:modlocator")       // Forge v3/v4 mod locator
include(":platform:forge:transformers")     // Forge ASM transformers
include(":platform:forge:v1")               // Forge [1.7.10]
include(":platform:forge:v2")               // Forge [1.8, 1.16.5]
include(":platform:forge:v3")               // Forge [1.17, 1.20.4]
include(":platform:forge:v4")               // Forge [1.20.6,)

// platform modules — NeoForge
include(":platform:neoforge:modlocator")    // NeoForge mod locator
include(":platform:neoforge:v1")            // NeoForge [1.20.2,)

// platform modules — Vanilla (+ OptiFine)
include(":platform:vanilla:agent")          // Vanilla java agent
include(":platform:vanilla:loader")         // Mixin without a mod loader
include(":platform:vanilla:tweaker")        // LaunchWrapper tweaker
include(":platform:vanilla:v1")             // Vanilla [1.6, 1.14)
include(":platform:vanilla:v2")             // Vanilla [1.14, 26.1)
include(":platform:vanilla:v3")             // Vanilla [26.1,)

// platform cross-module shared code
include(":shared:asm", ":shared:mixin")

// cross-version facades for some libraries
include(":facade:authlib", ":facade:fml_loader", ":facade:fml_spi", ":facade:forgespi", ":facade:neoforgespi")

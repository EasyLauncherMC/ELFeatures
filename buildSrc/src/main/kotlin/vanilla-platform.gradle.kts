import elfeatures.gradle.model.ModuleSpec

plugins {
    id("fabric-loom")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

loom {
    mixin {
        defaultRefmapName = "${spec.mod.id}.refmap.json"
//        messages = mapOf(
//            "NO_OBFDATA_FOR_METHOD" to "disabled",
//            "TARGET_ELEMENT_NOT_FOUND" to "disabled"
//        )
    }
}

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

tasks.remapJar {
    isPreserveFileTimestamps = true
}
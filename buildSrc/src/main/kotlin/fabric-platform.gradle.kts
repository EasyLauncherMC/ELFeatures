import elfeatures.gradle.model.Mod

plugins {
    id("fabric-loom")
}

// load Mod info
val mod: Mod = rootProject.extra["mod"] as Mod

// configure resources processing
tasks.processResources {
    // not needed for fabric
    exclude("elfeatures_banner.png")
}
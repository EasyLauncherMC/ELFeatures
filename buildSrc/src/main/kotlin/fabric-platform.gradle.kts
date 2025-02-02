plugins {
    id("fabric-loom")
}

// configure resources processing
tasks.processResources {
    // not needed for fabric
    exclude("elfeatures_banner.png")
}
import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    `fabric-platform`
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

loom {
    mixin {
        defaultRefmapName = "${spec.mod.id}.refmap.json"
        messages = mapOf(
            "NO_OBFDATA_FOR_METHOD" to "disabled",
            "TARGET_ELEMENT_NOT_FOUND" to "disabled"
        )
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${spec.props["minecraft_version"]}")
    modImplementation("net.fabricmc:fabric-loader:${spec.props["loader_version"]}")

    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${spec.props["yarn_mappings"]}:v2")
        mappings(project.layout.projectDirectory.file("extra-mappings.tiny"))
    })

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor(libs.lombok)
}

tasks.remapJar {
    isPreserveFileTimestamps = true
}
import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    id("net.fabricmc.fabric-loom-remap") version "1.16-SNAPSHOT"
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

loom {
    mixin {
        defaultRefmapName = "${spec.mod.id}.refmap.json"
        useLegacyMixinAp = true

        messages = mapOf(
            "NO_OBFDATA_FOR_METHOD" to "warning",
            "NO_OBFDATA_FOR_TARGET" to "warning",
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

tasks {
    jar {
        // populate JAR with mod icon
        from(rootProject.layout.projectDirectory.dir("resources")) {
            include("assets/**")
        }
    }

    remapJar {
        isPreserveFileTimestamps = true
    }
}

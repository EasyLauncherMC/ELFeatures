import elfeatures.gradle.model.Mod
import java.util.*

plugins {
    java
    id("elfeatures")
    `base-platform`
    `fabric-platform`
    publish
}

val mod: Mod = rootProject.extra["mod"] as Mod
val props: Properties = project.extra["props"] as Properties

val moduleName  : String        by extra { "fabric" }
val usedModules : List<String>  by extra { listOf("core", "shared:mixin") }
val modFiles    : List<String>  by extra { listOf("fabric.mod.json", "quilt.mod.json") }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

loom {
    mixin {
        defaultRefmapName = "${mod.id}.refmap.json"
        messages = mapOf(
            "NO_OBFDATA_FOR_METHOD" to "disabled",
            "TARGET_ELEMENT_NOT_FOUND" to "disabled"
        )
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${props["minecraft_version"]}")
    modImplementation("net.fabricmc:fabric-loader:${props["loader_version"]}")

    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${props["yarn_mappings"]}:v2")
        mappings(project.layout.projectDirectory.file("extra-mappings.tiny"))
    })

    usedModules.forEach { implementation(project(":${it}")) }
    compileOnly(project(":facade:authlib"))

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.remapJar {
    isPreserveFileTimestamps = true
}

// configure publishing
publishing {
    publications {
        named<MavenPublication>("maven") {
            artifact(tasks.remapJar) {
                classifier = null
            }
        }
    }
}
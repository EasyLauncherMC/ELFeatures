import elfeatures.gradle.model.Mod
import java.util.*

plugins {
    java
    id("elfeatures")
    `base-platform`
    `forge-platform`
    id("org.spongepowered.mixin")
    publish
}

val mod: Mod = rootProject.extra["mod"] as Mod
val props: Properties = project.extra["props"] as Properties

val moduleName  : String        by extra { "forge-v3" }
val usedModules : List<String>  by extra { listOf("core", "shared:mixin") }
val modFiles    : List<String>  by extra { listOf("META-INF/mods.toml") }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

mixin {
    add(sourceSets.main.get(), "${mod.id}.refmap.json")
    config("${mod.id}.mixins.json")
    extraMappings("build/mappings/mixin.tsrg")
    quiet()
}

dependencies {
    minecraft("net.minecraftforge:forge:${props["minecraft_version"]}-${props["forge_version"]}") { isChanging = false }

    usedModules.forEach { implementation(project(":${it}")) }
    compileOnly(project(":facade:authlib"))

    annotationProcessor("org.spongepowered:mixin:0.8.4:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

// configure publishing
publishing {
    publications {
        named<MavenPublication>("maven") {
            artifact(tasks.shadowPlatformJar) {
                classifier = null
            }
        }
    }
}
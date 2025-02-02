import elfeatures.gradle.model.Mod
import java.util.*

plugins {
    java
    id("elfeatures")
    `base-platform`
    `forge-platform`
    publish
}

val mod: Mod = rootProject.extra["mod"] as Mod
val props: Properties = project.extra["props"] as Properties

val moduleName  : String        by extra { "forge-v2" }
val usedModules : List<String>  by extra { listOf("core", "platform:forge:transformers", "shared:asm") }
val modFiles    : List<String>  by extra { listOf("mcmod.info", "META-INF/mods.toml") }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

dependencies {
    minecraft("net.minecraftforge:forge:${props["minecraft_version"]}-${props["forge_version"]}")

    usedModules.forEach { implementation(project(":${it}")) }

    compileOnly("net.minecraft:launchwrapper:1.12")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.jar {
    manifest {
        attributes(
            "FMLCorePlugin" to "org.easylauncher.mods.elfeatures.ELFeaturesFMLPlugin",
            "FMLCorePluginContainsFMLMod" to "true",
        )
    }
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
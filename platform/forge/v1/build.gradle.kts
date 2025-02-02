import java.util.*

plugins {
    java
    id("elfeatures")
    `base-platform`
    `forge-platform`
    publish
}

val props: Properties = project.extra["props"] as Properties

val moduleName  : String        by extra { "forge-v1" }
val usedModules : List<String>  by extra { listOf("core", ":platform:forge:transformers", "shared:asm") }
val modFiles    : List<String>  by extra { listOf("mcmod.info") }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

dependencies {
    minecraft("net.minecraftforge:forge:${props["minecraft_version"]}-${props["forge_version"]}")

    usedModules.forEach { implementation(project(":${it}")) }

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
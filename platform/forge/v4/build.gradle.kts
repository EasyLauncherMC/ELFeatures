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

val moduleName  : String        by extra { "forge-v4" }
val usedModules : List<String>  by extra { listOf("core", "shared:mixin") }
val modFiles    : List<String>  by extra { listOf("META-INF/mods.toml") }

// publishing properties
ext.set("publishJarTaskName", "transformJarContentAfterShadow")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

mixin {
    config("${mod.id}.mixins.json")
    reobfSrgFile = "build/mappings/mixin.tsrg"

    messages["MIXIN_SOFT_TARGET_NOT_RESOLVED"] = "disabled"
    messages["TARGET_ELEMENT_NOT_FOUND"] = "disabled"
    showMessageTypes()
    quiet()
}

dependencies {
    minecraft("net.minecraftforge:forge:${props["minecraft_version"]}-${props["forge_version"]}") { isChanging = false }

    usedModules.forEach { implementation(project(":${it}")) }
    compileOnly(project(":facade:authlib"))

    annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")?.let { compileOnly(it) }
    jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")?.let { implementation(it) { jarJar.ranged(it, "[0.4.1,)") } }

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.jar {
    manifest {
        attributes("MixinConfigs" to "elfeatures.mixins.json")
    }
}
import elfeatures.gradle.model.Mod
import java.util.*

plugins {
    java
    id("elfeatures")
    `base-platform`
    `neoforge-platform`
    publish
}

val mod: Mod = rootProject.extra["mod"] as Mod
val props: Properties = project.extra["props"] as Properties

val moduleName  : String        by extra { "neoforge" }
val usedModules : List<String>  by extra { listOf("core", "shared:mixin") }
val modFiles    : List<String>  by extra { listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml") }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

dependencies {
    implementation("net.neoforged:neoforge:${props["neoforge_version"]}")

    usedModules.forEach { implementation(project(":${it}")) }
    compileOnly(project(":facade:authlib"))

    annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")?.let { compileOnly(it) }
    compileOnly("io.github.llamalad7:mixinextras-neoforge:0.4.1")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.compileJava {
    val tsrgFile = project.layout.buildDirectory.dir("mappings").get().file("mixin.tsrg").asFile

    options.compilerArgs.addAll(listOf(
        "-AreobfTsrgFile=${tsrgFile.canonicalPath}",
        "-AmappingTypes=tsrg",
        "-AMSG_MIXIN_SOFT_TARGET_NOT_RESOLVED=disabled",
        "-AMSG_TARGET_ELEMENT_NOT_FOUND=disabled",
        "-AshowMessageTypes=true",
        "-Aquiet=true",
    ))
}

tasks.jar {
    manifest {
        attributes("MixinConfigs" to "elfeatures.mixins.json")
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
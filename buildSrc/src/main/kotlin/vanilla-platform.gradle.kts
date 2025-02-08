import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec

plugins {
    id("fabric-loom")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

loom {
    mixin {
        defaultRefmapName = "${spec.mod.id}.refmap.json"
//        messages = mapOf(
//            "NO_OBFDATA_FOR_METHOD" to "disabled",
//            "TARGET_ELEMENT_NOT_FOUND" to "disabled"
//        )
    }
}

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    minecraft("com.mojang:minecraft:${spec.props["minecraft_version"]}")

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    implementation("io.github.llamalad7:mixinextras-common:0.4.1")?.let { annotationProcessor(it) }
    implementation("net.fabricmc:mapping-io:0.7.1")
    implementation("net.fabricmc:sponge-mixin:0.15.0+mixin.0.8.7")

    compileOnly("net.minecraft:launchwrapper:1.12")

    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.named<ShadowJar>("shadowPlatformJar") {
    dependencies {
        include(dependency("io.github.llamalad7:mixinextras-common"))
        include(dependency("net.fabricmc:mapping-io"))
    }

    mergeServiceFiles()

    relocate("com.llamalad7.mixinextras", "${project.group}.libs.mixinextras")
    relocate("net.fabricmc.mappingio", "${project.group}.libs.mappingio")
}

tasks.jar {
    manifest {
        attributes("MixinConfigs" to "elfeatures.mixins.json")
    }
}

tasks.remapJar {
    isPreserveFileTimestamps = true
}
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi

plugins {
    java
    id("elfeatures")
    id("base-platform")
    id("net.fabricmc.fabric-loom-remap")
    id("ploceus")
    id("publish")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec
val ploceus = extensions.getByName("ploceus") as PloceusGradleExtensionApi

java {
    toolchain.languageVersion = JavaLanguageVersion.of(spec.javaVersion)
}

ploceus.setIntermediaryGeneration(2)

loom {
    runs.clear()

    mixin {
        useLegacyMixinAp = true
        defaultRefmapName = "${spec.mod.id}.refmap.json"

        messages = mapOf(
            "NO_OBFDATA_FOR_METHOD" to "warning",
            "NO_OBFDATA_FOR_TARGET" to "warning",
            "TARGET_ELEMENT_NOT_FOUND" to "disabled"
        )
    }
}

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    minecraft("com.mojang:minecraft:${spec.props["minecraft_version"]}")

    mappings(ploceus.layeredMappings {
        mappings("net.ornithemc:feather-gen2:${spec.props["minecraft_version"]}+build.${spec.props["feather_build"]}:v2") {
            containsUnpick()
        }

        mappings(project.layout.projectDirectory.file("extra-mappings.tiny"))
    })

    spec.addUsedModules(this)

    // the merged game jar marks client-only members with @Environment, which lives in the loader; a plain compileOnly
    // keeps loom from taking the loader's installer data, and the shadow JAR only bundles what it includes by name
    compileOnly("net.fabricmc:fabric-loader:0.16.0") { isTransitive = false }

    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    annotationProcessor(libs.lombok)
}

tasks {
    // Loom drops extra mapping entries without official names from the final mappings.tiny,
    // but mappings-base.tiny retains them. Redirect the mixin AP to use mappings-base.tiny instead
    compileJava {
        doFirst {
            options.compilerArgs = options.compilerArgs.map { arg ->
                if (arg.startsWith("-AinMapFileNamedIntermediary="))
                    arg.replace("mappings.tiny", "mappings-base.tiny")

                else arg
            }
        }
    }

    jar {
        manifest {
            attributes("Premain-Class" to "org.easylauncher.mods.elfeatures.ELFeaturesAgent")
        }
    }

    // no loader ships mixin here, so the platform carries its own — relocated as a guard: an agent is appended to the
    // end of the system class path, and any ASM or mixin already on it would be handed to us instead of this one
    named<ShadowJar>("shadowPlatformJar") {
        dependencies {
            include(dependency("io.github.llamalad7:mixinextras-common"))
            include(dependency("net.fabricmc:mapping-io"))
            include(dependency("net.fabricmc:sponge-mixin"))
            include(dependency("org.ow2.asm:.*:.*"))
        }

        mergeServiceFiles()

        relocate("com.llamalad7.mixinextras", "${project.group}.libs.mixinextras")
        relocate("net.fabricmc.mappingio", "${project.group}.libs.mappingio")
        relocate("org.objectweb.asm", "${project.group}.libs.asm")
        relocate("org.spongepowered.asm", "${project.group}.libs.spongepowered")
    }
}

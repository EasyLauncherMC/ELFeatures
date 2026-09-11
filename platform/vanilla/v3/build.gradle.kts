import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    id("base-platform")
    id("net.fabricmc.fabric-loom")
    id("publish")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

java {
    toolchain.languageVersion = JavaLanguageVersion.of(spec.javaVersion)
}

loom {
    // the agent is a client-side thing only, so the client's own classes belong on the one source set there is
    clientOnlyMinecraftJar()

    // the launcher starts the game with an agent, not loom with a loader: with no run configurations left, the
    // IDE sync stops setting up launches (assets, natives, run configurations) too
    runs.clear()
}

// no mappings and no refmap: the game stopped being obfuscated with 26.1, so the mixins already name it the
// way it runs and nothing has to be remapped on either side
dependencies {
    minecraft("com.mojang:minecraft:${spec.props["minecraft_version"]}")

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor(libs.lombok)
}

tasks {
    jar {
        manifest {
            attributes("Premain-Class" to "org.easylauncher.mods.elfeatures.ELFeaturesAgent")

            attributes(
                mapOf("Implementation-Version" to libs.versions.asm.get()),
                "${project.group}.libs.asm".replace('.', '/') + "/",
            )
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

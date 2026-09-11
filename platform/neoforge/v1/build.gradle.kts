import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    id("net.neoforged.gradle.userdev") version "7.1.21"
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

subsystems {
    parchment {
        mappingsVersion = spec.props["mappings_version"]?.toString()
        minecraftVersion = spec.props["minecraft_version"]?.toString()
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${spec.props["neoforge_version"]}")

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")?.let { compileOnly(it) }
    compileOnly("io.github.llamalad7:mixinextras-neoforge:0.4.1")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor(libs.lombok)
}

tasks {
    compileJava {
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

    jar {
        manifest {
            attributes("MixinConfigs" to "elfeatures.mixins.json")
        }

        // populate JAR with mod banner
        from(rootProject.layout.projectDirectory.dir("resources")) {
            include("elfeatures_banner.png")
        }
    }
}

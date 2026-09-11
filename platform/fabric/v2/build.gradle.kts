import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    id("base-platform")
    id("net.fabricmc.fabric-loom")
    id("publish")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

loom {
    splitEnvironmentSourceSets()

    mods {
        create(spec.mod.id) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${spec.props["minecraft_version"]}")
    implementation("net.fabricmc:fabric-loader:${spec.props["loader_version"]}")

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor(libs.lombok)
}

tasks {
    jar {
        // populate JAR with mod icon
        from(rootProject.layout.projectDirectory.dir("resources")) {
            include("assets/**")
        }
    }
}

import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    `forge-platform`
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

dependencies {
    minecraft("net.minecraftforge:forge:${spec.props["minecraft_version"]}-${spec.props["forge_version"]}")

    spec.addUsedModules(this)

    annotationProcessor(libs.lombok)
}

tasks.jar {
    manifest {
        attributes(
            "FMLCorePlugin" to "org.easylauncher.mods.elfeatures.ELFeaturesFMLPlugin",
            "FMLCorePluginContainsFMLMod" to "true",
        )
    }
}
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
    implementation(minecraft.dependency("net.minecraftforge:forge:${spec.props["minecraft_version"]}-${spec.props["forge_version"]}")) {
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.scala-lang.plugins")
    }

    spec.addUsedModules(this)

    compileOnly("net.minecraft:launchwrapper:1.12")

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
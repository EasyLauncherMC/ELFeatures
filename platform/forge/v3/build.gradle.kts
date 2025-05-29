import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    `forge-platform`
    id("org.spongepowered.mixin")
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

mixin {
    add(sourceSets.main.get(), "${spec.mod.id}.refmap.json")
    config("${spec.mod.id}.mixins.json")
    extraMappings("build/mappings/mixin.tsrg")
    quiet()
}

dependencies {
    minecraft("net.minecraftforge:forge:${spec.props["minecraft_version"]}-${spec.props["forge_version"]}") { isChanging = false }

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor("org.spongepowered:mixin:0.8.4:processor")
    annotationProcessor(libs.lombok)
}
import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    id("base-platform")
    id("forge-platform")
    id("org.spongepowered.mixin")
    id("publish")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${spec.props["minecraft_version"]}-${spec.props["forge_version"]}"))

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    annotationProcessor(libs.lombok)
}

mixin {
    add(sourceSets.main.get(), "${spec.mod.id}.refmap.json")
    config("${spec.mod.id}.mixins.json")
    reobfSrgFile = layout.buildDirectory.file("mappings/official2srg.tsrg").get().asFile.absolutePath
    // targets that don't exist in 1.17.1 (1.20.2+ SkinManager) are only mapped here
    extraMappings(layout.buildDirectory.file("mappings/mixin.tsrg").get().asFile.absolutePath)
    messages["ACCESSOR_TARGET_NOT_FOUND"] = "disabled"
    messages["NO_OBFDATA_FOR_METHOD"] = "warning"
    messages["NO_OBFDATA_FOR_TARGET"] = "warning"
    messages["TARGET_ELEMENT_NOT_FOUND"] = "disabled"
    quiet()
}
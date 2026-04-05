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
    messages["NO_OBFDATA_FOR_METHOD"] = "warning"
    messages["NO_OBFDATA_FOR_TARGET"] = "warning"
    messages["TARGET_ELEMENT_NOT_FOUND"] = "disabled"
    quiet()
}
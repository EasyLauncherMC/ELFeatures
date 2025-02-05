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
    config("${spec.mod.id}.mixins.json")
    reobfSrgFile = "build/mappings/mixin.tsrg"

    messages["MIXIN_SOFT_TARGET_NOT_RESOLVED"] = "disabled"
    messages["TARGET_ELEMENT_NOT_FOUND"] = "disabled"
    showMessageTypes()
    quiet()
}

dependencies {
    minecraft("net.minecraftforge:forge:${spec.props["minecraft_version"]}-${spec.props["forge_version"]}") { isChanging = false }

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")?.let { compileOnly(it) }
    jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")?.let { implementation(it) { jarJar.ranged(it, "[0.4.1,)") } }

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.jar {
    manifest {
        attributes("MixinConfigs" to "elfeatures.mixins.json")
    }
}
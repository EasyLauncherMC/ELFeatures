import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    `neoforge-platform`
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

dependencies {
    implementation("net.neoforged:neoforge:${spec.props["neoforge_version"]}")

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")?.let { compileOnly(it) }
    compileOnly("io.github.llamalad7:mixinextras-neoforge:0.4.1")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.compileJava {
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

tasks.jar {
    manifest {
        attributes("MixinConfigs" to "elfeatures.mixins.json")
    }
}
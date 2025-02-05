import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    `vanilla-platform`
    id("legacy-looming")
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

dependencies {
    minecraft("com.mojang:minecraft:${spec.props["minecraft_version"]}")

    mappings(loom.layered {
        mappings("net.legacyfabric:yarn:${spec.props["yarn_mappings"]}:v2")
        mappings(project.layout.projectDirectory.file("extra-mappings.tiny"))
    })

    spec.addUsedModules(this)
    compileOnly(project(":facade:authlib"))

    implementation("io.github.llamalad7:mixinextras-common:0.4.1")?.let { annotationProcessor(it) }
    implementation("org.spongepowered:mixin:0.8.7")

    compileOnly("net.minecraft:launchwrapper:1.12")

    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

//tasks.compileJava {
//    val tsrgFile = project.layout.buildDirectory.dir("mappings").get().file("mixin.tsrg").asFile
//
//    options.compilerArgs.addAll(listOf(
//        "-AreobfTsrgFile=${tsrgFile.canonicalPath}",
//        "-AmappingTypes=tsrg",
////        "-AMSG_MIXIN_SOFT_TARGET_NOT_RESOLVED=disabled",
////        "-AMSG_TARGET_ELEMENT_NOT_FOUND=disabled",
//        "-AshowMessageTypes=true",
//        "-Aquiet=true",
//    ))
//}
//
//tasks.jar {
//    manifest {
//        attributes("MixinConfigs" to "elfeatures.mixins.json")
//    }
//}
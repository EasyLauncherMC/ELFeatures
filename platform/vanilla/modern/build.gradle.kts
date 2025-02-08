import elfeatures.gradle.model.ModuleSpec

plugins {
    java
    id("elfeatures")
    `base-platform`
    `vanilla-platform`
    publish
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

dependencies {
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${spec.props["yarn_mappings"]}:v2")
        mappings(project.layout.projectDirectory.file("extra-mappings.tiny"))
    })
}
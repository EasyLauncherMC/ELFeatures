rootProject.name = "build-logic"

// mixingradle fork compatible with Gradle 9.+
includeBuild("../platform/forge/mixingradle") {
    dependencySubstitution {
        substitute(module("org.spongepowered:mixingradle")).using(project(":"))
    }
}

plugins {
    java
    id("net.neoforged.gradle.userdev")
}

tasks.jar {
    // populate JAR with mod banner
    from(rootProject.layout.projectDirectory.dir("resources")) {
        include("elfeatures_banner.png")
    }
}

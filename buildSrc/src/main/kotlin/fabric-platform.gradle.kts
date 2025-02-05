plugins {
    id("fabric-loom")
}

tasks.jar {
    // populate JAR with mod icon
    from(rootProject.layout.projectDirectory.dir("resources")) {
        include("assets/**")
    }
}
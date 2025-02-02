plugins {
    id("fabric-loom")
}

// configure JAR processing
tasks.jar {
    // populate JAR with mod icon
    from(rootProject.layout.projectDirectory.dir("resources")) {
        include("assets/**")
    }
}
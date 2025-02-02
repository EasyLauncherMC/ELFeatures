plugins {
    java
}

// change default output JARs name
base.archivesName = "elfeatures-modlocator"

// publishing properties
ext.set("publishJarArtifactId", "${base.archivesName.get()}-${project.name}")
ext.set("publishJarTaskName", "jar")

// configure JAR packaging
tasks.jar {
    archiveClassifier = project.name
    archiveVersion = ""
    destinationDirectory = rootProject.layout.buildDirectory
    includeEmptyDirs = false

    exclude(listOf(
        "com/mojang/authlib/**",
        "cpw/mods/fml/**",
        "net/minecraft/**",
        "net/minecraftforge/**",
        "net/neoforged/**"
    ))
}
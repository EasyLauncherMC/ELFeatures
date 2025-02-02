plugins {
    java
}

// change default output JARs name
base.archivesName = "elfeatures-modlocator"

// publishing properties
ext.set("publishJarTaskName", "jar")

// configure JAR packaging
tasks.jar {
    archiveAppendix = project.name
    includeEmptyDirs = false

    exclude(listOf(
        "com/mojang/authlib/**",
        "cpw/mods/fml/**",
        "net/minecraft/**",
        "net/minecraftforge/**",
        "net/neoforged/**"
    ))
}
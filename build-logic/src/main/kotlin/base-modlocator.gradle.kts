
import elfeatures.gradle.model.ModuleSpec
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    java
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

base.archivesName = "${spec.mod.id}-${spec.moduleName}"

tasks.jar {
    includeEmptyDirs = false

    exclude(listOf(
        "com/mojang/authlib/**",
        "cpw/mods/fml/**",
        "net/minecraft/**",
        "net/minecraftforge/**",
        "net/neoforged/**"
    ))

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    manifest {
        attributes(
            "Automatic-Module-Name"     to  "${spec.mod.id}-modlocator",
            "Specification-Title"       to  spec.mod.name,
            "Specification-Vendor"      to  spec.mod.authors,
            "Specification-Version"     to  1, // we're version 1 of ourselves
            "Implementation-Title"      to  spec.mod.name,
            "Implementation-Version"    to  spec.mod.version,
            "Implementation-Vendor"     to  spec.mod.authors,
            "Implementation-Timestamp"  to  formatter.format(LocalDateTime.now(ZoneOffset.UTC)),
        )
    }

    doLast {
        copy {
            from(archiveFile)
            into(rootProject.layout.buildDirectory)
            rename { name -> name.replace("-${spec.mod.version}", "") }
        }
    }
}
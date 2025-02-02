import elfeatures.gradle.model.Mod
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    java
    id("net.neoforged.gradle.userdev")
}

// load Mod info
val mod: Mod = rootProject.extra["mod"] as Mod

// configure JAR manifest content
tasks.jar {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    manifest {
        attributes(
            "Automatic-Module-Name"     to  mod.id,
            "Specification-Title"       to  mod.name,
            "Specification-Vendor"      to  mod.authors,
            "Specification-Version"     to  1, // we're version 1 of ourselves
            "Implementation-Title"      to  mod.name,
            "Implementation-Version"    to  mod.version,
            "Implementation-Vendor"     to  mod.authors,
            "Implementation-Timestamp"  to  formatter.format(LocalDateTime.now(ZoneOffset.UTC)),
        )
    }
}

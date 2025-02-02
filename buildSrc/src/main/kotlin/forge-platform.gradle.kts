
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.Mod
import elfeatures.gradle.tasks.TransformJarContentTask
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.base
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.ext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    java
    id("net.minecraftforge.gradle")
}

// load properties and Mod info
val props: Properties = project.extra["props"] as Properties
val mod: Mod = rootProject.extra["mod"] as Mod

val enableJarJar = props["enable_jarjar"]?.toString()?.toBooleanStrictOrNull()?:false
if (enableJarJar) {
    jarJar.enable()
}

// publishing properties
ext.set("publishJarArtifactId", "${base.archivesName.get()}-forge-${project.name}")

minecraft {
    mappings(props["mappings_channel"].toString(), props["mappings_version"].toString())
    val enableReobf = props["enable_reobf"]?.toString()?.toBooleanStrictOrNull()?:true
    if (!enableReobf) {
        reobf = false
    }
}

if (enableJarJar) {
    val transformJarSuffix = ".fuck_jengelman"

    tasks.register<TransformJarContentTask>("transformJarContentBeforeShadow") {
        from(zipTree(tasks.jarJar.get().archiveFile))
        archiveAppendix = "before-shadow"
        archiveBaseName = tasks.jarJar.get().archiveBaseName
        destinationDirectory = layout.buildDirectory.dir("tmp")
        dependsOn(tasks.jarJar)

        fileEntryNameTransformer { name ->
            if (name.endsWith(".jar"))
                "$name$transformJarSuffix"
            else
                name
        }
    }

    tasks.named<ShadowJar>("shadowPlatformJar") {
        archiveAppendix = "after-shadow"
        archiveClassifier = null
        destinationDirectory = layout.buildDirectory.dir("tmp")
        dependsOn("transformJarContentBeforeShadow")
    }

    tasks.register<TransformJarContentTask>("transformJarContentAfterShadow") {
        from(zipTree(tasks.named<ShadowJar>("shadowPlatformJar").get().archiveFile))
        archiveClassifier = project.extra["moduleName"] as String
        destinationDirectory = rootProject.layout.buildDirectory
        dependsOn("shadowPlatformJar")

        fileEntryNameTransformer { name ->
            if (name.endsWith(".jar$transformJarSuffix"))
                name.substring(0, name.length - transformJarSuffix.length)
            else
                name
        }
    }
}

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

    if (enableJarJar) {
        finalizedBy("transformJarContentAfterShadow")
    } else if (minecraft.reobf) {
        finalizedBy("reobfJar")
    }
}

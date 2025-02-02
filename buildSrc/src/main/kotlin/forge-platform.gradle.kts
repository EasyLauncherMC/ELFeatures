
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.tasks.TransformJarContentTask
import gradle.kotlin.dsl.accessors._523dc74e2e9552463686721a7434f18b.jar
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.base
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.ext
import java.util.*

plugins {
    java
    id("net.minecraftforge.gradle")
}

// load properties and Mod info
val props: Properties = project.extra["props"] as Properties

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
        archiveVersion = ""
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

// configure JAR processing
tasks.jar {
    // populate JAR with mod banner
    from(rootProject.layout.projectDirectory.dir("resources")) {
        include("elfeatures_banner.png")
    }

    if (enableJarJar) {
        finalizedBy("transformJarContentAfterShadow")
    } else if (minecraft.reobf) {
        finalizedBy("reobfJar")
    }
}

if (enableJarJar) {
    // configure Jar-in-Jar processing
    tasks.jarJar {
        // populate JAR with mod banner
        from(rootProject.layout.projectDirectory.dir("resources")) {
            include("elfeatures_banner.png")
        }
    }
}

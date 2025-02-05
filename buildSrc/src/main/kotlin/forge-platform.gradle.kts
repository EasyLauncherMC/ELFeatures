import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec
import elfeatures.gradle.task.TransformJarContentTask

plugins {
    java
    id("net.minecraftforge.gradle")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

val enableJarJar = spec.props["enable_jarjar"]?.toString()?.toBooleanStrictOrNull()?:false
if (enableJarJar) {
    jarJar.enable()
}

minecraft {
    mappings(spec.props["mappings_channel"].toString(), spec.props["mappings_version"].toString())
    val enableReobf = spec.props["enable_reobf"]?.toString()?.toBooleanStrictOrNull()?:true
    if (!enableReobf) {
        reobf = false
    }
}

if (enableJarJar) {
    val transformJarSuffix = ".fuck_jengelman"

    tasks.register<TransformJarContentTask>("transformJarContentBeforeShadow") {
        from(zipTree(tasks.jarJar.get().archiveFile))
        archiveBaseName = tasks.jarJar.get().archiveBaseName
        archiveClassifier = "before-shadow"
        destinationDirectory = layout.buildDirectory.dir("tmp")
        dependsOn(tasks.jarJar)

        fileEntryNameTransformer { name: String ->
            if (name.endsWith(".jar"))
                "$name$transformJarSuffix"
            else
                name
        }
    }

    tasks.named<ShadowJar>("shadowPlatformJar") {
        archiveClassifier = "after-shadow"
        destinationDirectory = layout.buildDirectory.dir("tmp")
        dependsOn("transformJarContentBeforeShadow")
    }

    tasks.register<TransformJarContentTask>("transformJarContentAfterShadow") {
        from(zipTree(tasks.named<ShadowJar>("shadowPlatformJar").get().archiveFile))
        archiveClassifier = "all"
        destinationDirectory = tasks.jar.get().destinationDirectory
        dependsOn("shadowPlatformJar")

        fileEntryNameTransformer { name: String ->
            if (name.endsWith(".jar$transformJarSuffix"))
                name.substring(0, name.length - transformJarSuffix.length)
            else
                name
        }

        doLast {
            copy {
                from(archiveFile)
                into(rootProject.layout.buildDirectory)
                rename { name -> name.replace("-${spec.mod.version}", "").replace("-all", "") }
            }
        }
    }
}

tasks.jar {
    // populate JAR with mod banner
    from(rootProject.layout.projectDirectory.dir("resources")) {
        include("elfeatures_banner.png")
    }

    if (enableJarJar) {
        finalizedBy(spec.publishJarTask)
    } else if (minecraft.reobf) {
        finalizedBy("reobfJar")
    }
}

if (enableJarJar) {
    tasks.jarJar {
        // populate JAR with mod banner
        from(rootProject.layout.projectDirectory.dir("resources")) {
            include("elfeatures_banner.png")
        }
    }
}

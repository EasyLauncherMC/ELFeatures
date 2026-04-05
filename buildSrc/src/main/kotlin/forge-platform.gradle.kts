import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec
import elfeatures.gradle.task.TransformJarContentTask

plugins {
    java
    id("net.minecraftforge.gradle")
    id("net.minecraftforge.jarjar")
    id("net.minecraftforge.renamer")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

val enableJarJar = spec.props["enable_jarjar"]?.toString()?.toBooleanStrictOrNull() ?: false
val enableReobf = spec.props["enable_reobf"]?.toString()?.toBooleanStrictOrNull() ?: true

repositories {
    mavenCentral()
    minecraft.mavenizer(repositories)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

minecraft {
    mappings(spec.props["mappings_channel"].toString(), spec.props["mappings_version"].toString())
}

if (enableJarJar) {
    jarJar.register()
    val transformJarSuffix = ".fuck_jengelman"

    tasks.register<TransformJarContentTask>("transformJarContentBeforeShadow") {
        from(zipTree(tasks.named("jarJar").map { (it as AbstractArchiveTask).archiveFile }.get()))
        archiveBaseName = (tasks.named("jarJar").get() as AbstractArchiveTask).archiveBaseName
        archiveClassifier = "before-shadow"
        destinationDirectory = layout.buildDirectory.dir("tmp")
        dependsOn("jarJar")

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

if (enableReobf) {
    renamer.classes(tasks.named("jar", Jar::class.java)) {
        afterEvaluate {
            map.from(minecraft.dependency.toSrgFile)
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
    } else if (enableReobf) {
        finalizedBy("renameJar")
    }
}

if (enableJarJar) {
    tasks.named("jarJar") {
        // populate JAR with mod banner
        (this as Jar).from(rootProject.layout.projectDirectory.dir("resources")) {
            include("elfeatures_banner.png")
        }
    }
}

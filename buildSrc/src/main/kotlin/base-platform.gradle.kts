@file:Suppress("UNCHECKED_CAST")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.Mod
import elfeatures.gradle.tasks.InjectConstantsTask
import gradle.kotlin.dsl.accessors._523dc74e2e9552463686721a7434f18b.jar
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.base
import gradle.kotlin.dsl.accessors._8c47cae829ea3d03260d5ff13fb2398e.ext
import org.gradle.kotlin.dsl.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

// load properties and Mod info
val props: Properties = project.extra["props"] as Properties
val mod: Mod = rootProject.extra["mod"] as Mod

val enableJarJar = props["enable_jarjar"]?.toString()?.toBooleanStrictOrNull()?:false

// change default output JARs name
base.archivesName = mod.id

// publishing properties
ext.set("publishJarArtifactId", "${base.archivesName.get()}-${project.name}")
ext.set("publishJarTaskName", "shadowPlatformJar")

// configure Constants class injecting
tasks.named<InjectConstantsTask>("injectConstants") {
    outputClassName.set("${project.group}.Constants")
    constants.put("MOD_NAME", mod.name)
    constants.put("MOD_VERSION", mod.version)
}

// configure shading
tasks.register<ShadowJar>("shadowPlatformJar") {
    dependencies {
        exclude { dep -> dep.moduleGroup != rootProject.group }
        (project.extra["usedModules"] as List<*>).forEach { include(project(":${it}")) }
    }

    exclude(
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "module-info.class"
    )

    val moduleName = project.extra["moduleName"] as String
    var jarFile = ((if (moduleName == "fabric") tasks.getByName("remapJar") else tasks.jar.get()) as AbstractArchiveTask).archiveFile

    if (enableJarJar) {
        jarFile = (tasks.getByName("transformJarContentBeforeShadow") as AbstractArchiveTask).archiveFile
    }

    // construct shadow JAR from compiled JAR file instead of source-set output
    from(zipTree(jarFile))
    configurations = listOf(project.configurations.compileClasspath.get())
    manifest.inheritFrom(tasks.jar.get().manifest)

    archiveClassifier = moduleName
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

// configure JAR processing
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

// configure resources filtering
tasks.processResources {
    val replacements = mod.toReplaceProperties()
    inputs.properties(replacements)

    val modFiles: List<String> = project.extra["modFiles"] as List<String>
    filesMatching(modFiles) {
        expand(replacements)
    }
}

// configure mappings extraction
val mixinMappings: Path = project.layout.projectDirectory.file("mixin.tsrg").asFile.toPath()
if (Files.isRegularFile(mixinMappings)) {
    tasks.create("extractTsrgMappings") {
        doLast {
            logger.info("Extracting TSRG mixin mappings...")
            copy {
                from(projectDir) {
                    include("mixin.tsrg")
                }

                into(project.layout.buildDirectory.dir("mappings").get())
            }
        }
    }

    tasks.compileJava {
        dependsOn("extractTsrgMappings")
    }
}

// shade after build
tasks.build {
    finalizedBy("shadowPlatformJar")
}
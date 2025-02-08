import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec
import elfeatures.gradle.task.InjectConstantsTask
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    java
    id("com.github.johnrengelman.shadow")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

base.archivesName = "${spec.mod.id}-${spec.moduleName}"

tasks.named<InjectConstantsTask>("injectConstants") {
    outputClassName = "${project.group}.Constants"
    constants = mapOf(
        "MOD_NAME"      to spec.mod.name,
        "MOD_VERSION"   to spec.mod.version
    )
}

tasks.register<ShadowJar>("shadowPlatformJar") {
    dependencies {
        spec.usedModules.forEach { include(project(":${it}")) }
    }

    exclude(
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "module-info.class"
    )

    // construct shadow JAR from compiled JAR file instead of source-set output
    from(zipTree((tasks.getByName(spec.baseJarTask) as AbstractArchiveTask).archiveFile))
    configurations = listOf(project.configurations.compileClasspath.get())
    manifest.inheritFrom(tasks.jar.get().manifest)

    archiveClassifier = "all"
    includeEmptyDirs = false

    exclude(listOf(
        "com/mojang/authlib/**",
        "cpw/mods/fml/**",
        "net/minecraft/**",
        "net/minecraftforge/**",
        "net/neoforged/**",
        "LICENSE*"
    ))

    if (spec.publishJarTask == "shadowPlatformJar") {
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
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    manifest {
        attributes(
            "Automatic-Module-Name"     to  spec.mod.id,
            "Specification-Title"       to  spec.mod.name,
            "Specification-Vendor"      to  spec.mod.authors,
            "Specification-Version"     to  1, // we're version 1 of ourselves
            "Implementation-Title"      to  spec.mod.name,
            "Implementation-Version"    to  spec.mod.version,
            "Implementation-Vendor"     to  spec.mod.authors,
            "Implementation-Timestamp"  to  formatter.format(LocalDateTime.now(ZoneOffset.UTC)),
        )
    }
}

tasks.processResources {
    val replacements = spec.mod.toReplaceProperties()
    inputs.properties(replacements)

    if (spec.resources.isNotEmpty()) {
        filesMatching(spec.resources) {
            expand(replacements)
        }
    }
}

// configure mappings extraction
val mixinMappings: Path = project.layout.projectDirectory.file("mixin.tsrg").asFile.toPath()
if (Files.isRegularFile(mixinMappings)) {
    tasks.register("extractTsrgMappings") {
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

tasks.build {
    finalizedBy(spec.publishJarTask)
}
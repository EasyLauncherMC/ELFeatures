import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import elfeatures.gradle.model.ModuleSpec
import elfeatures.gradle.task.InjectConstantsTask
import elfeatures.gradle.task.VerifyMappingsTask
import net.minecraftforge.renamer.gradle.RenameJar
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    java
    id("com.gradleup.shadow")
}

val spec: ModuleSpec = ext["spec"] as ModuleSpec

// disable default shadowJar task — we use shadowPlatformJar instead
tasks.named<ShadowJar>("shadowJar") {
    enabled = false
}

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
        // a vanilla platform carries the loader itself, so it names what to bundle by including it
        if (!spec.moduleName.startsWith("vanilla"))
            exclude { dep -> dep.moduleGroup != rootProject.group }

        spec.usedModules.forEach { include(project(":${it}")) }
    }

    // shadow defaults to EXCLUDE, which drops the copies the service-file transformer is there to merge
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    exclude(
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "module-info.class",
        "fernflower_abstract_parameter_names.txt" // renamer leftover, only useful to a decompiler
    )

    // construct shadow JAR from compiled JAR file instead of source-set output
    from(zipTree(jarFileOf(spec.baseJarTask)))
    configurations = listOf(project.configurations.compileClasspath.get())
    manifest.from(tasks.jar.get().manifest)

    archiveClassifier = "all"
    includeEmptyDirs = false

    exclude(listOf(
        "com/mojang/authlib/**",
        "cpw/mods/fml/**",
        "net/minecraft/**",
        "net/minecraftforge/**",
        "net/neoforged/**"
    ))

    if (spec.publishJarTask == "shadowPlatformJar") {
        doLast {
            copy {
                from(archiveFile)
                into(rootProject.layout.buildDirectory)
                rename { name -> name.replace("-${project.version}", "").replace("-all", "") }
            }
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name"     to  spec.mod.id,
            "Specification-Title"       to  spec.mod.name,
            "Specification-Vendor"      to  spec.mod.authors,
            "Specification-Version"     to  1, // we're version 1 of ourselves
            "Implementation-Title"      to  spec.mod.name,
            "Implementation-Version"    to  spec.mod.version,
            "Implementation-Vendor"     to  spec.mod.authors,
            "Implementation-Timestamp"  to  LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME),
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
    val extractMappingsTask = tasks.register<Copy>("extractTsrgMappings") {
        description = "Copies mixin.tsrg into build/mappings, where the mixin annotation processor reads it from"

        from(layout.projectDirectory.file("mixin.tsrg"))
        into(layout.buildDirectory.dir("mappings"))
    }

    tasks.compileJava {
        dependsOn(extractMappingsTask)
    }
}

tasks.assemble {
    dependsOn(spec.publishJarTask)
}

// guard against shipping a JAR that still calls Minecraft by its development names
val verifyMappings = tasks.register<VerifyMappingsTask>("verifyMappings") {
    group = "verification"
    description = "Verifies that the published JAR calls Minecraft by the names its runtime actually has"

    jarFile = jarFileOf(spec.publishJarTask)
    namingScheme = requireNotNull(spec.runtimeNames) {
        "'runtime_names' is not set in ${project.path} build.properties (official, srg, intermediary or calamus)"
    }
}

tasks.check {
    dependsOn(verifyMappings)
}

// JAR is produced either by an archive task (jar, remapJar, ...) or by the renamer (renameJar)
fun jarFileOf(taskName: String): Provider<RegularFile> = tasks.named(taskName).flatMap { task ->
    when (task) {
        is AbstractArchiveTask -> task.archiveFile
        is RenameJar -> task.output
        else -> error("task '$taskName' produces no JAR file to build the shadow JAR from")
    }
}
import elfeatures.gradle.model.Mod
import elfeatures.gradle.model.ModuleSpec
import java.util.*

plugins {
    base
    id("elfeatures") apply false
    id("net.fabricmc.fabric-loom") version "1.17.20" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.17.20" apply false
    id("ploceus") version "1.17.7" apply false
}

val branch = providers.environmentVariable("GITHUB_REF_NAME").orNull ?: resolveBranchOrNull()
val mod = Mod(loadProperties(project))

group = "org.easylauncher.mods.elfeatures"
version = if (branch == "main") mod.version else "${mod.version}-SNAPSHOT"

// provide group, version and build-properties to all subprojects
subprojects {
    group = rootProject.group
    version = rootProject.version

    val props = loadProperties(project)
    extra["spec"] = ModuleSpec.of(mod, props)
}

fun loadProperties(project: Project): Properties {
    val file = project.file("build.properties")
    val props = Properties()

    if (file.isFile()) {
        props.apply {
            load(file.reader())
        }
    }

    return props
}

private fun resolveBranchOrNull(): String =
    providers.exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()

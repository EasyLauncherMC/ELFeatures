import elfeatures.gradle.model.Mod
import java.util.*

plugins {
    base
}

val props = loadProperties(project)
val mod by extra(Mod(props))

group = "org.easylauncher.mods.elfeatures"
version = mod.version

// provide group, version and build-properties to all subprojects
subprojects {
    group = rootProject.group
    version = rootProject.version

    val props by extra(loadProperties(project))
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
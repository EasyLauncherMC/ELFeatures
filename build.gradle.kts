import elfeatures.gradle.model.Mod
import elfeatures.gradle.model.ModuleSpec
import java.util.*

plugins {
    base
}

val mod by extra(Mod(loadProperties(project)))

group = "org.easylauncher.mods.elfeatures"
version = mod.version

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
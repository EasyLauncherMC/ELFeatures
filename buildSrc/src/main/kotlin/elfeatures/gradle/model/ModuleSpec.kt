package elfeatures.gradle.model

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project
import java.util.*

data class ModuleSpec(
    val mod: Mod,
    val props: Properties,
    val moduleName: String,
    val usedModules: List<String>,
    val resources: List<String>,
    val javaVersion: Int,
    val baseJarTask: String,
    val publishJarTask: String,
) {

    fun addUsedModules(dependencyHandler: DependencyHandler): List<Dependency?> {
        return usedModules.stream()
            .map { dep -> ":$dep" }
            .map(dependencyHandler::project)
            .map { dep -> dependencyHandler.add("implementation", dep)}
            .toList()
    }

    companion object {

        fun of(mod: Mod, props: Properties): ModuleSpec? {
            val moduleName = props.getProperty("module_name") ?: return null

            val usedModules: List<String> = parseListProperty(props, "used_modules", ",")
            val resources: List<String> = parseListProperty(props, "resources", ";")

            val javaVersion: Int = props.getProperty("java_version")?.toIntOrNull() ?: 8
            val baseJarTask = props.getProperty("base_jar_task", "jar")
            val publishJarTask = props.getProperty("publish_jar_task", "shadowPlatformJar")

            return ModuleSpec(mod, props, moduleName, usedModules, resources, javaVersion, baseJarTask, publishJarTask)
        }

        private fun parseListProperty(
            props: Properties,
            property: String,
            separator: String
        ): List<String> {
            if (property !in props)
                return emptyList()

            return props[property].toString().split(separator).toList()
        }

    }
}
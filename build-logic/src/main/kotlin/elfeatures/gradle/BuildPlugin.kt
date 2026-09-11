package elfeatures.gradle

import elfeatures.gradle.task.InjectConstantsTask
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar

import java.io.File

class BuildPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val objects: ObjectFactory = project.objects
        val buildDir: File = project.layout.buildDirectory.get().asFile

        val injectedSourcesLocation = FileUtils.getFile(buildDir, "generated", "sources", "injectConstants")
        val injectConstantsTask: TaskProvider<InjectConstantsTask> = project.tasks.register(
            "injectConstants",
            InjectConstantsTask::class.java
        ) {
            group = TASK_GROUP_TOOLS
            outputDir.set(injectedSourcesLocation)
        }

        val sourceSets: SourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val injectedSourceSet: SourceSet = sourceSets.create("injectedConstants") {
            val collection = objects.fileCollection()
                .from(injectedSourcesLocation)
                .builtBy(injectConstantsTask)

            java.setSrcDirs(collection)
        }

        project.tasks
            .named(injectedSourceSet.compileJavaTaskName)
            .configure {
                dependsOn(injectConstantsTask)
            }

        val mainSet: SourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val extraSources: SourceSetOutput = injectedSourceSet.output
        mainSet.compileClasspath += extraSources
        mainSet.runtimeClasspath += extraSources

        project.tasks.named("jar", Jar::class.java) {
            from(extraSources.asFileTree)
        }
    }

    companion object {
        private const val TASK_GROUP_TOOLS = "ELFeatures Tools"
    }

}
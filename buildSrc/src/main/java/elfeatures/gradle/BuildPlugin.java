package elfeatures.gradle;

import elfeatures.gradle.tasks.InjectConstantsTask;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.io.File;

public final class BuildPlugin implements Plugin<Project> {

    private static final String TASK_GROUP_TOOLS = "ELFeatures Tools";

    @Override
    public void apply(Project project) {
        ObjectFactory objects = project.getObjects();
        File buildDir = project.getLayout().getBuildDirectory().get().getAsFile();

        File injectedSourcesLocation = FileUtils.getFile(buildDir, "generated", "sources", "injectConstants");
        TaskProvider<InjectConstantsTask> injectConstantsTask = project.getTasks().register(
                "injectConstants",
                InjectConstantsTask.class,
                task -> {
                    task.setGroup(TASK_GROUP_TOOLS);
                    task.getOutputDir().set(injectedSourcesLocation);
                }
        );

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet injectedSourceSet = sourceSets.create(
                "injectedConstants",
                set -> set.getJava().setSrcDirs(objects.fileCollection()
                        .from(injectedSourcesLocation)
                        .builtBy(injectConstantsTask)
                )
        );

        project.getTasks()
                .named(injectedSourceSet.getCompileJavaTaskName())
                .configure(task -> task.dependsOn(injectConstantsTask));

        SourceSet mainSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSetOutput extraSources = injectedSourceSet.getOutput();
        mainSet.setCompileClasspath(mainSet.getCompileClasspath().plus(extraSources));
        mainSet.setRuntimeClasspath(mainSet.getRuntimeClasspath().plus(extraSources));

        project.getTasks().named("jar", Jar.class).configure(task -> task.from(extraSources.getAsFileTree()));
    }

}

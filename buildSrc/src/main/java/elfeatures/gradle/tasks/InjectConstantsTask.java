package elfeatures.gradle.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
Based on RetroFuturaGradle 'inject tags' task <a href="https://github.com/GTNewHorizons/RetroFuturaGradle/blob/master/plugin/src/main/java/com/gtnewhorizons/retrofuturagradle/mcp/InjectTagsTask.java">source code</a>
 */
@CacheableTask
public abstract class InjectConstantsTask extends DefaultTask {

    @Input
    @Optional
    public abstract MapProperty<String, Object> getConstants();

    @Input
    @Optional
    public abstract Property<String> getOutputClassName();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Input
    public abstract Property<Boolean> getCleanOutputDir();

    @Inject
    public InjectConstantsTask() {
        getCleanOutputDir().convention(true);
    }

    @TaskAction
    public void injectConstants() throws IOException {
        File outDir = getOutputDir().get().getAsFile();
        Map<String, Object> replacements = getConstants().get();
        String outClass = StringUtils.stripToNull(getOutputClassName().getOrNull());

        if (outClass != null) {
            int lastDot = outClass.lastIndexOf('.');
            String outPackage = (lastDot >= 0) ? outClass.substring(0, lastDot) : null;
            String outClassName = (lastDot >= 0) ? outClass.substring(lastDot + 1) : outClass;
            String outPath = (outPackage == null ? "" : outPackage.replace('.', '/') + "/") + outClassName + ".java";

            Directory outputDir = getOutputDir().get();
            if (getCleanOutputDir().get() && outputDir.getAsFile().isDirectory()) {
                try {
                    FileUtils.deleteDirectory(outputDir.getAsFile());
                } catch (IOException e) {
                    getLogger().warn("Could not clean output directory {}", outputDir.getAsFile(), e);
                }
            }

            File outFile = outputDir.file(outPath).getAsFile();
            FileUtils.forceMkdirParent(outFile);

            StringBuilder outWriter = new StringBuilder();
            if (outPackage != null)
                outWriter.append(String.format("package %s;\n\n", outPackage));

            outWriter.append("// Auto-generated constants from ELFeatures gradle plugin\n");
            outWriter.append(String.format("public class %s {\n\n", outClassName)); // class
            outWriter.append(String.format("    private %s() {}\n\n", outClassName)); // private constructor

            for (Map.Entry<String, Object> entry : replacements.entrySet()) {
                String identifier = entry.getKey();
                if (!isValidJavaIdentifier(identifier))
                    throw new InvalidUserDataException(String.format(
                            "Tag injection identifier %s isn't a valid Java identifier!",
                            identifier
                    ));

                Object rawValue = entry.getValue();
                String valueType, javaValue;

                if (rawValue instanceof Integer) {
                    valueType = "int";
                    javaValue = Integer.toString((Integer) rawValue);
                } else {
                    valueType = "String";
                    javaValue = String.format("\"%s\"", StringEscapeUtils.escapeJava(rawValue.toString()));
                }

                outWriter.append("    // Auto-generated constant from ELFeatures gradle plugin\n");
                outWriter.append(String.format("    public static final %s %s = %s;\n\n", valueType, identifier, javaValue));
            }

            outWriter.append("}\n");
            FileUtils.writeStringToFile(outFile, outWriter.toString(), StandardCharsets.UTF_8);
        }
    }

    private static boolean isValidJavaIdentifier(final String s) {
        if (s.isEmpty())
            return false;

        if (!Character.isJavaIdentifierStart(s.charAt(0)))
            return false;

        for (int i = 1; i < s.length(); i++)
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;

        return true;
    }

}

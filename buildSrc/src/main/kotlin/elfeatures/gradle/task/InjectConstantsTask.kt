package elfeatures.gradle.task

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.StringEscapeUtils
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * Based on RetroFuturaGradle 'inject tags' task [source code](https://github.com/GTNewHorizons/RetroFuturaGradle/blob/master/plugin/src/main/java/com/gtnewhorizons/retrofuturagradle/mcp/InjectTagsTask.java)
 */
@CacheableTask
abstract class InjectConstantsTask @Inject constructor() : DefaultTask() {

    @get:Optional
    @get:Input
    abstract val constants: MapProperty<String, Any>

    @get:Optional
    @get:Input
    abstract val outputClassName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val cleanOutputDir: Property<Boolean>

    init {
        this.cleanOutputDir.convention(true)
    }

    @TaskAction
    @Throws(IOException::class)
    fun injectConstants() {
        val replacements: Map<String, Any> = constants.get()
        val outClass: String = StringUtils.stripToNull(outputClassName.getOrNull())

        val lastDot = outClass.lastIndexOf('.')
        val outPackage = if (lastDot >= 0) outClass.substring(0, lastDot) else null
        val outClassName = if (lastDot >= 0) outClass.substring(lastDot + 1) else outClass
        val outPath = (if (outPackage == null) "" else outPackage.replace('.', '/') + "/") + outClassName + ".java"

        val outputDir = outputDir.get()
        if (cleanOutputDir.get() && outputDir.asFile.isDirectory()) {
            try {
                FileUtils.deleteDirectory(outputDir.asFile)
            } catch (e: IOException) {
                logger.warn("Could not clean output directory {}", outputDir.asFile, e)
            }
        }

        val outFile = outputDir.file(outPath).asFile
        FileUtils.forceMkdirParent(outFile)

        val outWriter = StringBuilder()
        if (outPackage != null) outWriter.append(String.format("package %s;\n\n", outPackage))

        outWriter.append("// Auto-generated constants from ELFeatures gradle plugin\n")
        outWriter.append("public class $outClassName {\n\n")    // class
        outWriter.append("    private $outClassName() {}\n\n")  // private constructor

        replacements.let {
            for (entry in it.entries) {
                val identifier: String = entry.key
                if (!isValidJavaIdentifier(identifier))
                    throw InvalidUserDataException("Tag injection identifier $identifier isn't a valid Java identifier!")

                val rawValue: Any = entry.value
                val valueType: String?
                val javaValue: String?

                if (rawValue is Int) {
                    valueType = "int"
                    javaValue = rawValue.toString()
                } else {
                    valueType = "String"
                    javaValue = String.format("\"%s\"", StringEscapeUtils.escapeJava(rawValue.toString()))
                }

                outWriter.append("    // Auto-generated constant from ELFeatures gradle plugin\n")
                outWriter.append("    public static final $valueType $identifier = $javaValue;\n\n")
            }
        }

        outWriter.append("}\n")
        FileUtils.writeStringToFile(outFile, outWriter.toString(), StandardCharsets.UTF_8)
    }

    companion object {
        private fun isValidJavaIdentifier(s: String): Boolean {
            if (s.isEmpty())
                return false

            if (!Character.isJavaIdentifierStart(s[0]))
                return false

            for (i in 1 until s.length)
                if (!Character.isJavaIdentifierPart(s[i]))
                    return false

            return true
        }
    }
}
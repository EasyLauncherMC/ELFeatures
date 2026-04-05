package elfeatures.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.zip.GZIPInputStream

abstract class DecompressSrgFileTask : DefaultTask() {

    @get:InputFile
    @get:Optional
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun decompress() {
        val src = inputFile.get().asFile
        val dst = outputFile.get().asFile
        dst.parentFile.mkdirs()
        if (src.name.endsWith(".gz")) {
            src.inputStream().use { fis ->
                GZIPInputStream(fis).use { gis ->
                    dst.outputStream().use { fos ->
                        gis.copyTo(fos)
                    }
                }
            }
        } else {
            src.copyTo(dst, overwrite = true)
        }
    }

}

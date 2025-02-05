package elfeatures.gradle.task

import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.UnixStat
import org.gradle.api.GradleException
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.CopyActionProcessingStreamAction
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.WorkResults
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.function.UnaryOperator

class TransformJarContentCopyAction(
    private val jarFile: Path,
    private val fileEntryNameTransformer: UnaryOperator<String>,
    private val dirEntryNameTransformer: UnaryOperator<String>
) : CopyAction {

    override fun execute(stream: CopyActionProcessingStream): WorkResult {
        val zipOutStr: JarArchiveOutputStream

        try {
            if (!Files.isDirectory(jarFile.parent))
                Files.createDirectories(jarFile.parent)

            zipOutStr = JarArchiveOutputStream(
                Files.newOutputStream(
                    jarFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            )
        } catch (ex: Exception) {
            throw GradleException("Couldn't create JAR '$jarFile'", ex)
        }

        try {
            zipOutStr.use { stream.process(StreamAction(it)) }
        } catch (ex: Exception) {
            try {
                Files.deleteIfExists(jarFile)
            } catch (_: IOException) {
            }
            throw ex
        }

        return WorkResults.didWork(true)
    }

    private inner class StreamAction(
        private val jarOutStr: JarArchiveOutputStream
    ) : CopyActionProcessingStreamAction {

        override fun processFile(details: FileCopyDetailsInternal) {
            if (details.isDirectory) {
                visitDir(details)
            } else {
                visitFile(details)
            }
        }

        fun visitFile(fileDetails: FileCopyDetails) {
            try {
                val entryName = fileEntryNameTransformer.apply(fileDetails.relativePath.getPathString())
                val archiveEntry = JarArchiveEntry(entryName)
                archiveEntry.time = fileDetails.lastModified
                archiveEntry.setUnixMode(UnixStat.FILE_FLAG or fileDetails.permissions.toUnixNumeric())
                jarOutStr.putArchiveEntry(archiveEntry)
                fileDetails.copyTo(jarOutStr)
                jarOutStr.closeArchiveEntry()
            } catch (ex: Exception) {
                throw GradleException("Couldn't add $fileDetails to JAR '$jarFile'", ex)
            }
        }

        fun visitDir(dirDetails: FileCopyDetails) {
            try {
                val entryName = dirEntryNameTransformer.apply(dirDetails.relativePath.getPathString() + '/')
                val archiveEntry = JarArchiveEntry(entryName)
                archiveEntry.time = dirDetails.lastModified
                archiveEntry.setUnixMode(UnixStat.DIR_FLAG or dirDetails.permissions.toUnixNumeric())
                jarOutStr.putArchiveEntry(archiveEntry)
                jarOutStr.closeArchiveEntry()
            } catch (ex: Exception) {
                throw GradleException("Couldn't add $dirDetails to JAR '$jarFile'", ex)
            }
        }
    }
}
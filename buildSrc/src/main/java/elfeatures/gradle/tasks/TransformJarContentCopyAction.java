package elfeatures.gradle.tasks;

import lombok.AllArgsConstructor;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.internal.IoActions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.UnaryOperator;

@AllArgsConstructor
public class TransformJarContentCopyAction implements CopyAction {

    private final Path jarFile;
    private final UnaryOperator<String> fileEntryNameTransformer;
    private final UnaryOperator<String> dirEntryNameTransformer;

    @Override
    public WorkResult execute(CopyActionProcessingStream stream) {
        final JarArchiveOutputStream zipOutStr;

        try {
            if (!Files.isDirectory(jarFile.getParent()))
                Files.createDirectories(jarFile.getParent());

            zipOutStr = new JarArchiveOutputStream(Files.newOutputStream(jarFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
        } catch (Exception ex) {
            throw new GradleException(String.format("Could not create JAR '%s'.", jarFile), ex);
        }

        try {
            Action<? super JarArchiveOutputStream> streamAction = outputStream -> stream.process(new StreamAction(outputStream));
            IoActions.withResource(zipOutStr, streamAction);
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(jarFile);
            } catch (IOException ignored) {
            }
            throw ex;
        }

        return WorkResults.didWork(true);
    }

    @AllArgsConstructor
    private class StreamAction implements CopyActionProcessingStreamAction {

        private final JarArchiveOutputStream jarOutStr;

        @Override
        public void processFile(FileCopyDetailsInternal details) {
            if (details.isDirectory()) {
                visitDir(details);
            } else {
                visitFile(details);
            }
        }

        private void visitFile(FileCopyDetails fileDetails) {
            try {
                String entryName = fileEntryNameTransformer.apply(fileDetails.getRelativePath().getPathString());
                JarArchiveEntry archiveEntry = new JarArchiveEntry(entryName);
                archiveEntry.setTime(fileDetails.getLastModified());
                archiveEntry.setUnixMode(UnixStat.FILE_FLAG | fileDetails.getPermissions().toUnixNumeric());
                jarOutStr.putArchiveEntry(archiveEntry);
                fileDetails.copyTo(jarOutStr);
                jarOutStr.closeArchiveEntry();
            } catch (Exception ex) {
                throw new GradleException(String.format("Could not add %s to JAR '%s'.", fileDetails, jarFile), ex);
            }
        }

        private void visitDir(FileCopyDetails dirDetails) {
            try {
                String entryName = dirEntryNameTransformer.apply(dirDetails.getRelativePath().getPathString() + '/');
                JarArchiveEntry archiveEntry = new JarArchiveEntry(entryName);
                archiveEntry.setTime(dirDetails.getLastModified());
                archiveEntry.setUnixMode(UnixStat.DIR_FLAG | dirDetails.getPermissions().toUnixNumeric());
                jarOutStr.putArchiveEntry(archiveEntry);
                jarOutStr.closeArchiveEntry();
            } catch (Exception ex) {
                throw new GradleException(String.format("Could not add %s to JAR '%s'.", dirDetails, jarFile), ex);
            }
        }

    }

}

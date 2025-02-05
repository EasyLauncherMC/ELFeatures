package elfeatures.gradle.task

import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.bundling.Zip
import org.gradle.work.DisableCachingByDefault
import java.util.function.UnaryOperator
import javax.inject.Inject

@DisableCachingByDefault(because = "Not worth caching")
abstract class TransformJarContentTask @Inject constructor() : Zip() {
    private var fileEntryNameTransformer: UnaryOperator<String>
    private var dirEntryNameTransformer: UnaryOperator<String>

    init {
        archiveExtension.set("jar")
        this.fileEntryNameTransformer = UnaryOperator.identity()
        this.dirEntryNameTransformer = UnaryOperator.identity()
    }

    override fun createCopyAction(): CopyAction {
        return TransformJarContentCopyAction(
            archiveFile.get().asFile.toPath(),
            fileEntryNameTransformer,
            dirEntryNameTransformer
        )
    }

    fun fileEntryNameTransformer(transformer: UnaryOperator<String>) {
        fileEntryNameTransformer = transformer
    }

    fun dirEntryNameTransformer(transformer: UnaryOperator<String>) {
        dirEntryNameTransformer = transformer
    }
}
package elfeatures.gradle.tasks;

import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.util.function.UnaryOperator;

@DisableCachingByDefault(because = "Not worth caching")
public abstract class TransformJarContentTask extends Zip {

    private UnaryOperator<String> fileEntryNameTransformer;
    private UnaryOperator<String> dirEntryNameTransformer;

    @Inject
    public TransformJarContentTask() {
        getArchiveExtension().set("jar");
        this.fileEntryNameTransformer = UnaryOperator.identity();
        this.dirEntryNameTransformer = UnaryOperator.identity();
    }

    @Override
    protected CopyAction createCopyAction() {
        return new TransformJarContentCopyAction(
                getArchiveFile().get().getAsFile().toPath(),
                fileEntryNameTransformer,
                dirEntryNameTransformer
        );
    }

    public void fileEntryNameTransformer(UnaryOperator<String> transformer) {
        this.fileEntryNameTransformer = transformer;
    }

    public void dirEntryNameTransformer(UnaryOperator<String> transformer) {
        this.dirEntryNameTransformer = transformer;
    }

}

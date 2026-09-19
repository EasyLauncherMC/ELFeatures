package cpw.mods.fml.relauncher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

public interface IFMLLoadingPlugin {

    String[] getLibraryRequestClass();

    String[] getASMTransformerClass();

    String getModContainerClass();

    String getSetupClass();

    void injectData(Map<String, Object> var1);

    String getAccessTransformerClass();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Name {
        String value() default "";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface SortingIndex {
        int value() default 0;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface TransformerExclusions {
        String[] value() default "";
    }

}

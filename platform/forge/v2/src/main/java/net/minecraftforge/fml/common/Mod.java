package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mod {

    String value();

    String modid() default "";

    String name() default "";

    String version() default "";

    boolean useMetadata() default false;

    boolean clientSideOnly() default false;

    String acceptedMinecraftVersions() default "";

}

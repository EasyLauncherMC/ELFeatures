package org.easylauncher.mods.elfeatures.core.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransformerTarget {

    String className();

    String methodNameSrg() default "";

    String[] methodNames() default {};

    String methodDesc() default "";

}

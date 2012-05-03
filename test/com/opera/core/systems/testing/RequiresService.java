package com.opera.core.systems.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures that the core service is version 2.1 or higher (which includes meta information).
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresService {

  String service();

  String version() default "";

}
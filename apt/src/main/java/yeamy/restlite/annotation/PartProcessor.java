package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the method to create http body part.<br>
 * For public static-method/constructor with one param (one of).<br>
 * Can throw a {@link ProcessException} to terminate an HTTP request
 *
 * @see Parts
 * @see PartProcessor
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface PartProcessor {

    /**
     * distinguish the constructor/static-method with same return type
     *
     * @return name of this processor
     */
    String value() default "";
}

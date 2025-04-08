package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the method to create http body.<br>
 * For public static-method/constructor with one param (one of)
 *
 * @see Body
 * @see BodyFactory
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface BodyProcessor {

    /**
     * distinguish the constructor/static-method with same return type
     *
     * @return name of this processor
     */
    String value() default "";
}

package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the method to create http body.<br>
 * For public static-method/constructor with one param (one of)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BodyFactory {

    /**
     * Class name of static factory class.
     */
    Class<?> processorClass();

    // name
    String processor();
}

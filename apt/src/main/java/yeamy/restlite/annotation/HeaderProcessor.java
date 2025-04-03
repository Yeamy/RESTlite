package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the method to create http param.<br>
 * Method with only one String param and return any type
 * Noted that the element must contain modifies public static.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface HeaderProcessor {

    // name
    String value() default "";
}

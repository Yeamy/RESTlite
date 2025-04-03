package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the method to create http header.<br>
 * For public static-method/constructor with one param (one of {@linkplain java.lang.String String},
 * {@linkplain java.lang.Integer Integer}/int, {@linkplain java.lang.Long Long}/long(timeMiles),
 * {@linkplain java.util.Date Date})
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface HeaderProcessor {

    // name
    String value() default "";
}

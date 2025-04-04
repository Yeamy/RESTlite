package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the method to create http cookie.<br>
 * For public static-method/constructor with one param (one of {@linkplain java.lang.String String},
 * {@linkplain jakarta.servlet.http.Cookie Cookie}, Cookie[])
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface CookieProcessor {

    // name
    String value() default "";
}

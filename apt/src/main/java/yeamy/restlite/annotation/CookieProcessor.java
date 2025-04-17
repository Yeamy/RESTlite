package yeamy.restlite.annotation;

import yeamy.restlite.addition.ProcessException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the method to create http cookie.<br>
 * For public static-method/constructor with one param (one of {@link String},
 * {@linkplain jakarta.servlet.http.Cookie Cookie}, Cookie[]).<br>
 * Can throw a {@link ProcessException} to terminate an HTTP request
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface CookieProcessor {

    /**
     * distinguish the constructor/static-method with same return type
     *
     * @return name of this processor
     */
    String value() default "";
}

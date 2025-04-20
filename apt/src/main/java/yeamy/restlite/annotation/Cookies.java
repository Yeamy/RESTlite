package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the parameter is http request cookies.<br>
 * <b>support param type:</b> {@linkplain String String},
 * {@linkplain jakarta.servlet.http.Cookie Cookies},
 * {@linkplain jakarta.servlet.http.Cookie Cookies[]}, any type with {@link Cookies#processor()}
 *
 * @author Yeamy
 * @see Header
 * @see Param
 * @see Body
 * @see Parts
 * @see Attribute
 * @see CookieProcessor
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Cookies {
    /**
     * @return same as {@link #name()}
     */
    String value() default "";

    /**
     * @return name of Cookies, can be empty if same with parameter
     */
    String name() default "";

    /**
     * @return Specify which {@link CookieProcessor} to create this http-body parameter
     */
    String processor() default "";
}

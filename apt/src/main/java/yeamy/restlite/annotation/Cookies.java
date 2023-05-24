package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http request cookies.<br>
 * <b>support type:</b> {@linkplain String String},
 * {@linkplain jakarta.servlet.http.Cookie Cookie[]}
 *
 * @author Yeamy
 * @see Header
 * @see Param
 * @see Body
 * @see Part
 * @see Attribute
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Cookies {
    /**
     * name of Cookies, keep empty if same with parameter
     */
    String value() default "";

    /**
     * class name of processor to preprocess value.<br>
     * <b>support executor:</b> constructor, static-method<br>
     * <b>support param:</b> only one {@linkplain jakarta.servlet.http.Cookie Cookie} or Cookie[]<br>
     * <b>support param:</b> any type
     *
     * @see #tag()
     */
    Class<?> processor() default void.class;

    /**
     * tag the constructor/static-method, suggest to use if more than one
     * constructor/method
     *
     * @see LinkTag
     */
    String tag() default "";
}

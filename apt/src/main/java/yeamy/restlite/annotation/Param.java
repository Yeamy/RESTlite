package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http uri parameter or query parameter.<br>
 * <b>Noted</b> that parameter(Java method) without annotation will be
 * treated as http parameter, too.<br>
 * <p>
 * support type: {@linkplain String String}, String[], long, long[],
 * int, int[], boolean, boolean[], {@linkplain java.math.BigDecimal BigDecimal},
 * BigDecimal[], {@linkplain yeamy.restlite.HttpRequestFile HttpRequestFile},
 * HttpRequestFile[], {@linkplain yeamy.restlite.RESTfulRequest RESTfulRequest},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 *
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Body
 * @see Part
 * @see Attribute
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    String value() default "";

    boolean required() default true;

    /**
     * class name of processor to create value.<br>
     * support executor: constructor, method<br>
     * support param: only one String
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

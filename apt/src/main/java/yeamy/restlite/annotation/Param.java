package yeamy.restlite.annotation;

import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * support type : {@linkplain String String}, String[], long, long[],
 * int, int[], boolean, boolean[], {@linkplain java.math.BigDecimal BigDecimal},
 * BigDecimal[], {@linkplain yeamy.restlite.HttpRequestFile HttpRequestFile},
 * HttpRequestFile[], {@linkplain RESTfulRequest RESTfulRequest},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 * 
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Body
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

	String value() default "";

	String fallback() default "";

	boolean required() default true;
}

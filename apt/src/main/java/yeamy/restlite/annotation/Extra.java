package yeamy.restlite.annotation;

import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Support Constructor Parameter: {@linkplain RESTfulRequest
 * RESTfulRequest}, {@linkplain jakarta.servlet.http.HttpServletRequest
 * HttpServletRequest};<br>
 * Support declared type with annotation {@link Creator}
 * 
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Param
 * @see Body
 * @see Creator
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Extra {

	/**
	 * class name of static factory class, if empty using type's {@link Creator},
	 * if no Creator using constructor
	 */
	String creator() default "";

	/**
	 * tag the constructor/creator-method, if more than one constructor/creator-method
	 */
	String tag() default "";

	String[] reference() default {};

	/**
	 * if sub-type of {@linkplain java.io.Closeable Closable}, invoke
	 * {@linkplain java.io.Closeable#close() close()} after try-catch
	 */
	boolean autoClose() default true;
}

package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Http method PUT</b><br>
 * support parameter :
 * {@linkplain Param Param},
 * {@linkplain Header Header},
 * {@linkplain Cookies Cookies},
 * {@linkplain Body Body}.<br>
 *
 * @author Yeamy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PUT {

	/**
	 * @return if asyncSupported
	 *
	 * @see jakarta.servlet.annotation.WebServlet
	 */
	boolean async() default false;

	/**
	 * @return async timeout in millisecond
	 */
	long asyncTimeout() default 0;

}

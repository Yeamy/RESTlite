package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http request header.<br>
 * <b>support type:</b> {@linkplain String String}, int, long(timeMiles), {@linkplain java.util.Date Date},
 * @author Yeamy
 * @see Cookies
 * @see Param
 * @see Body
 * @see Attribute
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Header {
	/** name of Header if same with parameter */
	String value() default "";
}

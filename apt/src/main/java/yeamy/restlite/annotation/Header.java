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
 * @see Part
 * @see Attribute
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Header {
	/** name of Header if same with parameter */
	String value() default "";

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

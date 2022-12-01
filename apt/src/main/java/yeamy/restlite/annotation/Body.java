package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * support type : {@linkplain java.io.InputStream InputStream},
 * {@linkplain javax.servlet.ServletInputStream ServletInputStream}, byte[],
 * {@linkplain String String} or any declared type with annotation
 * {@link Creator}
 * <br>
 * body can read only once per request
 * 
 * @author Yeamy
 * @see Extra
 * @see Header
 * @see Cookies
 * @see Param
 * @see Creator
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Body {

	/**
	 * class name of static factory class, if empty using type's {@link Creator},
	 * if no Creator using constructor
	 */
	String creator() default "";

	/**
	 * tag the constructor/creator-method, suggest to use if more than one
	 * constructor/method
	 * @see LinkTag
	 */
	String tag() default "";

	/**
	 * keep empty will be same as {@link Initialization}
	 */
	String charset() default "";
}

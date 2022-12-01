package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * support type : {@linkplain String String}
 * @author Yeamy
 * @see Extra
 * @see Cookies
 * @see Param
 * @see Body
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Header {
	/** name of Header if same with parameter */
	String value() default "";
}

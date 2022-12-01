package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * support type : {@linkplain String String},
 * {@linkplain jakarta.servlet.http.Cookie Cookie[]}
 * 
 * @author Yeamy
 * @see Extra
 * @see Header
 * @see Param
 * @see Body
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Cookies {
	/** name of Cookies if same with parameter */
	String value() default "";
}

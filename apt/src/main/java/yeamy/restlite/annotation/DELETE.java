package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP DELETE<br>
 * support parameter :
 * {@linkplain Param Param},
 * {@linkplain Header Header},
 * {@linkplain Cookies Cookies}, if no annotation
 * defend, take it as <b>Param</b>
 * 
 * @author Yeamy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DELETE {

	boolean async() default false;

	long asyncTimeout() default 0;

}

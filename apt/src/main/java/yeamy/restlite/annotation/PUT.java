package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP PUT<br>
 * support parameter : {@linkplain Extra Extra},
 * {@linkplain Param Param},
 * {@linkplain Header Header},
 * {@linkplain Cookies Cookies},
 * {@linkplain Body Body}.<br>
 * if no annotation defend, take it as <b>Param</b>
 * 
 * @author Yeamy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PUT {

	boolean async() default false;

	long asyncTimeout() default 0;

}

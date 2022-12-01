package yeamy.restlite.annotation;

import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>HTTP GET</b><br>
 * support parameter :
 * {@link Header},
 * {@link Cookies},
 * {@link Param},
 * {@link Extra},
 * {@link Body},
 * {@linkplain javax.servlet.http.HttpServletRequest HttpServletRequest},
 * {@linkplain RESTfulRequest RESTfulRequest},
 * if no annotation defend it will be treated as {@link Param}
 *
 * @author Yeamy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface GET {

    boolean async() default false;

    long asyncTimeout() default 0;

}

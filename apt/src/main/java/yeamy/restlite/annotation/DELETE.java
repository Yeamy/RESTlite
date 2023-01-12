package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Http method DELETE<br>
 * <b>support parameter:</b>
 * {@link Header},
 * {@link Cookies},
 * {@link Param},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 * {@linkplain yeamy.restlite.RESTfulRequest RESTfulRequest}
 *
 * @author Yeamy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DELETE {

    /**
     * asyncSupported
     *
     * @see jakarta.servlet.annotation.WebServlet
     */
    boolean async() default false;

    long asyncTimeout() default 0;

}

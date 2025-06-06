package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Http method GET</b><br>
 * support parameter :
 * {@link Header},
 * {@link Cookies},
 * {@link Param},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 * {@linkplain yeamy.restlite.RESTfulRequest RESTfulRequest}
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface GET {
    /**
     *
     * @return permission info of method
     * @see PermissionHandle
     */
    String permission() default "";

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

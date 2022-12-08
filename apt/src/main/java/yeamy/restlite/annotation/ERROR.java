package yeamy.restlite.annotation;

import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * call while other HTTP method throws Exception.<br>
 * support parameter type : {@linkplain RESTfulRequest RESTfulRequest},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 * {@linkplain Exception Exception}<br>
 * return type : {@linkplain yeamy.restlite.HttpResponse HttpResponse}
 * 
 * @author Yeamy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ERROR {

    boolean intercept() default true;
}

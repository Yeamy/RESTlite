package yeamy.restlite.annotation;

import yeamy.restlite.HttpResponse;
import yeamy.restlite.addition.ExceptionResponse;
import yeamy.restlite.RESTfulRequest;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare a method to catch all exceptions from any HTTP method.<br>
 * <b>support parameter type:</b> {@link RESTfulRequest},
 * {@link HttpServletRequest},
 * {@link HttpServlet},
 * {@link Exception}<br>
 * <b>return type:</b> if {@link HttpResponse} do write directly,
 * otherwise create a new {@link  ExceptionResponse}.
 *
 * @author Yeamy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ERROR {
}

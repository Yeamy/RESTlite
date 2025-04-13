package yeamy.restlite.annotation;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare a method to catch all exceptions from any HTTP method.<br>
 * <b>support parameter type:</b> {@link RESTfulRequest},
 * {@link HttpServletRequest},
 * {@link HttpServletResponse},
 * {@link HttpServlet},
 * {@link Exception}<br>
 *
 * <pre>{@code
 *
 * @ERROR
 * public static void doError(HttpServletResponse resp, Exception ex) {
 *     new TextPlainResponse(500, ex.getMessage()).write(resp);
 * }
 *
 * }</pre>
 *
 * @author Yeamy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ERROR {
}

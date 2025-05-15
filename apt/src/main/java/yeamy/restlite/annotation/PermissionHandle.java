package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the method to handle permission.<br>
 * <b>required param</b>: String (permission info), {@linkplain  yeamy.restlite.RESTfulRequest RESTfulRequest}
 * (or  {@linkplain  jakarta.servlet.http.HttpServletRequest HttpServletRequest}). <br>
 * <b>return</b>: {@linkplain  yeamy.restlite.HttpResponse HttpResponse}(return null when allow).<br>
 * <b>support throw type</b>: {@linkplain  jakarta.servlet.ServletException ServletException}, {@linkplain  java.io.IOException IOException}
 * <pre>{@code
 *
 * public static HttpResponse handlePermission(String permission, HttpServletRequest request) {
 *     Account account = MyAccountUtils.get(request);
 *     if (account.allow(permission)) return null;
 *     return new MyHttpResponse("deny");
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PermissionHandle {
}

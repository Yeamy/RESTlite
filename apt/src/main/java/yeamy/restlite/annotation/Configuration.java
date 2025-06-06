package yeamy.restlite.annotation;

import yeamy.restlite.HttpResponse;
import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * default Option of RESTlite
 *
 * @author Yeamy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Configuration {

    /**
     *
     * @return package name of application, if empty same with class annotated with @Configuration.
     */
    String packageName() default "";

    /**
     * Subclass of {@linkplain RESTfulRequest HttpResponse}, the class must have a one-parameter-constructor,
     * and the only one parameter will accept data.<br>
     * <br>
     * All service method({@link GET}, {@link POST},{@link PUT},{@link PATCH},{@link DELETE}) returned type will be
     * converted to {@linkplain yeamy.restlite.HttpResponse HttpResponse} for output:<br>
     * <br>
     * void → {@linkplain yeamy.restlite.addition.VoidResponse VoidResponse}<br>
     * Subtype of HttpResponse  → keep it<br>
     * others → currently set type, must have a constructor with two arguments one receive http-status-code(int) another
     *  one receive the method return result(any type).
     *
     * <pre>{@code
     *
     * public class MyResponse implements HttpResponse {
     *     private int status;
     *     private Object data;
     *
     *     public MyResponse(int status, Object data) {
     *         this.status = status;
     *         this.data = data;
     *     }
     *
     *     @Override
     *     public void write(HttpServletResponse resp) throws IOException {
     *         resp.setStatus(status);
     *         ... // do serialize here
     *     }
     * }
     * }</pre>
     *
     * @return subclass of HttpResponse
     * @see #responseAllType()
     */
    Class<? extends HttpResponse> response();

    /**
     * response() serialize all return type, default true.<br>
     * if set false, serialize by RESTlite:<br>
     * base type(int, long...) → {@linkplain yeamy.restlite.addition.TextPlainResponse TextPlainResponse}<br>
     * BigDecimal → {@linkplain yeamy.restlite.addition.TextPlainResponse TextPlainResponse}<br>
     * String → {@linkplain yeamy.restlite.addition.TextPlainResponse TextPlainResponse}<br>
     * OutputStream → {@linkplain yeamy.restlite.addition.StreamResponse StreamResponse}<br>
     *
     * @return if serialize all return type
     */
    boolean responseAllType() default true;

}

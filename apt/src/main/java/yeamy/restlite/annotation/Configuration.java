package yeamy.restlite.annotation;

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
     * set default response text encoding
     */
    String charset() default "UTF-8";

    /**
     * support http method PATCH (only support <b>Tomcat</b> yet)
     */
    SupportPatch supportPatch() default SupportPatch.undefined;

    /**
     * Subclass of {@linkplain RESTfulRequest HttpResponse}, the class must have a one-parameter-constructor,
     * and the only one parameter will accept data.<br>
     * <br>
     * All service method({@link GET}, {@link POST},{@link PUT},{@link PATCH},{@link DELETE}) returned type will be
     * converted to {@linkplain yeamy.restlite.HttpResponse HttpResponse} for output:<br>
     * <br>
     * void → {@linkplain yeamy.restlite.addition.VoidResponse VoidResponse}<br>
     * base type / BigDecimal / String → {@linkplain yeamy.restlite.addition.TextPlainResponse TextPlainResponse}<br>
     * OutputStream → {@linkplain yeamy.restlite.addition.StreamResponse StreamResponse}<br>
     * Subtype of HttpResponse  → keep it<br>
     * others → currently set type
     *
     * <pre>
     *
     * public class MyResponse implements HttpResponse {
     *
     *     &#64;Override
     *     public void write(HttpServletResponse resp) throws IOException {
     *         resp.setStatus(200);
     *         ... // do serialize here
     *     }
     * }
     * </pre>
     */
    String response();

}

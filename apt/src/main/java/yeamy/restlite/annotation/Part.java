package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter(or the type of parameter) is http request body.<br>
 * <b>support parameter:</b> {@linkplain java.io.InputStream InputStream}, byte[],
 * {@linkplain String String}, {@linkplain jakarta.servlet.http.Part Part}, Part[],
 * {@linkplain yeamy.restlite.HttpRequestFile HttpRequestFile}, HttpRequestFile[] or
 * any declared type with this annotation.<br>
 * <b>Noted</b> that only POST, PUT, PATCH contains multi-parts body
 * and each request part(InputStream) can read only once.
 *
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Param
 * @see Body
 * @see Attribute
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // ANNOTATION_TYPE need CLASS
public @interface Part {

    String value() default "";

    /**
     * Class name of static factory class. If empty, using type's Body annotation,
     * if still empty, using type's constructor.<br>
     * <b>support executor:</b> constructor, method<br>
     * <b>support param:</b> only one String<br>
     * <b>support param:</b> any type
     *
     */
    Class<?> processor() default void.class;

    /**
     * tag the constructor/static-method, suggest to use if more than one
     * constructor/method
     *
     * @see LinkTag
     */
    String tag() default "";

    /**
     * Only work when body is string rather than binary,
     * keep empty will be same as {@link Configuration}
     */
    String charset() default "";
}

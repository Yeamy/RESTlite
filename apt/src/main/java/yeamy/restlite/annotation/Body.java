package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter(or the type of parameter) is http request body.<br>
 * <b>support parameter:</b> {@linkplain java.io.InputStream InputStream},
 * {@linkplain jakarta.servlet.ServletInputStream ServletInputStream}, byte[],
 * {@linkplain String String} or any declared type with this annotation.<br>
 * <b>Noted</b> that only POST, PUT, PATCH contains body
 * and each request body(InputStream) can read only once.
 *
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Param
 * @see Part
 * @see Attribute
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // ANNOTATION_TYPE need CLASS
public @interface Body {

    /**
     * Class name of static factory class. If empty, using type's Body annotation,
     * if still empty, using type's constructor.<br>
     * <b>support executor:</b> constructor, static-method<br>
     * <b>support param:</b> {@linkplain jakarta.servlet.ServletInputStream ServletInputStream},
     * {@linkplain jakarta.servlet.http.Part Part[]},
     * {@linkplain yeamy.restlite.HttpRequestFile HttpRequestFile[]},<br>
     * <b>support param:</b> any type
     */
    Class<?> processor() default void.class;

    /**
     * tag the constructor/creator-method, suggest to use if more than one
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

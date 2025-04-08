package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter(or the type of parameter) is http request body.<br>
 * <b>support parameter:</b> {@linkplain java.io.InputStream InputStream},
 * {@linkplain jakarta.servlet.ServletInputStream ServletInputStream}, {@linkplain jakarta.servlet.http.Part Part[]}
 * {@linkplain yeamy.restlite.HttpRequestFile HttpRequestFile[]}, byte[], {@linkplain String String}
 * or any declared type with construct param as above.<br>
 * <b>Noted</b> that only POST, PUT, PATCH contains body
 * and each request body(InputStream) can read only once.
 *
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Param
 * @see Parts
 * @see Attribute
 * @see BodyProcessor
 * @see BodyFactory
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Body {

    /**
     * @return Specify which {@link BodyProcessor} to create this http-body parameter
     */
    String processor() default "";
}

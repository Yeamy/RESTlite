package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter(or the type of parameter) is http request body.<br>
 * <b>support parameter:</b> {@linkplain java.io.InputStream InputStream}, {@linkplain jakarta.servlet.http.Part Part},
 * {@linkplain yeamy.restlite.HttpRequestFile HttpRequestFile}, byte[], {@linkplain String String}
 * or any declared type with construct param as above.<br>
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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Parts {

    String value() default "";

    /**
     * distinguish the constructor/static-method with same return type
     *
     * @see PartProcessor
     */
    String processor() default "";
}

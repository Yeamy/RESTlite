package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http request cookies.<br>
 * <b>support type:</b> {@linkplain String String},
 * {@linkplain jakarta.servlet.http.Cookie Cookie[]}
 *
 * @author Yeamy
 * @see Header
 * @see Param
 * @see Body
 * @see Attribute
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Cookies {
    /**
     * name of Cookies, keep empty if same with parameter
     */
    String value() default "";

    /**
     * Process the value before method invoke.
     *
     * @return name of preprocessor
     * @see Preprocessor
     * @see yeamy.restlite.addition.ValueProcessor
     */
    String preprocess() default "";
}

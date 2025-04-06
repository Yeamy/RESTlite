package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http uri path parameter or query parameter.<br>
 * <b>Noted</b> that parameter(Java method) without annotation will be
 * treated as http parameter, too.<br>
 * <p>
 * support type: {@link String}, int, {@link Integer}, long, {@link Long}, float, {@link Float}, double, {@link Double},
 *  boolean, {@link Boolean}, {@linkplain java.math.BigDecimal BigDecimal}, an array of type as above
 *
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Body
 * @see Part
 * @see Attribute
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    String value() default "";

    boolean required() default true;

    /**
     * tag the constructor/method, suggest to use if more than one processor
     *
     * @see ParamProcessor
     */
    String processor() default "";
}

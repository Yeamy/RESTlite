package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the method to create http param.<br>
 * For public static-method/constructor with one param (one of {@link String}, int, {@link Integer}, long, {@link Long},
 * float, {@link Float}, double, {@link Double}, boolean, {@link Boolean}, {@linkplain java.math.BigDecimal BigDecimal},
 * an array of type as above)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ParamProcessor {

    // name
    String value() default "";
}

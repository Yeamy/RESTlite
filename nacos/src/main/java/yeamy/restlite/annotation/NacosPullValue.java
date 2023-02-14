package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * interface public method<br>
 * support int, long, short, float, double, boolean, String, BigDecimal
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface NacosPullValue {

    String group();

    String dataId();

    long timeoutMs();

    boolean autoRefreshed() default false;

//    ConfigType type() default ConfigType.UNSET;
}

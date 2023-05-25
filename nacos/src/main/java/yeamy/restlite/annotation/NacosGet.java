package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * interface getter method<br>
 * support int, long, short, float, double, boolean, String, BigDecimal
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface NacosGet {

    String group();

    String dataId();

    long timeoutMs();

    boolean autoRefreshed() default false;

    /**
     * class of processor to create value.<br>
     * <b>support executor:</b> constructor, method<br>
     * <b>support param:</b> only one String<br>
     * <b>support return:</b> any type
     */
    Class<?> processor() default void.class;

    /**
     * @see LinkTag
     */
    String tag() default "";

//    ConfigType type() default ConfigType.UNSET;
}

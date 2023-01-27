package yeamy.restlite.annotation;

import yeamy.utils.SingletonPool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare to inject the field of Resource.<br>
 * Noted that the injection instances will be created once and cached in the pool.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Inject {

    /**
     * Whether inject instance is singleton. Singleton object may not be closeable.<br>
     * <br>
     * <b>FIELD:</b><br>
     * <b>yes:</b> get or create from SingletonPool.<br>
     * <b>auto/no:</b> create new in servlet init(ServletConfig) and close in destroy()<br>
     * <br>
     * <b>PARAMETER:</b><br>
     * <b>auto/yes:</b> get or create from SingletonPool.<br>
     * <b>no:</b> create before method and close after method.<br>
     *
     * @see SingletonPool
     */
    Singleton singleton() default Singleton.auto;

    /**
     * class name of creator.<br>
     * <br>
     * <b>1.Lookup</b> field's creator() -> type's creator() -> @InjectProvider -> type's constructor<br>
     * <b>2.Lookup</b> tag() -> type's static field, method, none param public constructor -> null
     *
     * @see #tag()
     */
    String creator() default "";

    /**
     * tag the constructor/creator-method, suggest to use if more than one
     * constructor/method
     *
     * @see LinkTag
     */
    String tag() default "";
}

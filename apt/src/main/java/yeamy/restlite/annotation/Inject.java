package yeamy.restlite.annotation;

import yeamy.utils.SingletonPool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare to inject the field of {@link RESTfulResource}
 * or a param of http method such as {@link GET}, {@link POST}...<br>
 *
 * <b>1.Lookup</b> @InjectProvider -> type's constructor<br>
 * <b>2.Lookup</b> tag() -> type's static field, method, no param public constructor -> null
 *
 * @see Inject#singleton()
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
     * take it as no.<br>
     *
     * @return Whether inject instance is singleton.
     * @see SingletonPool
     */
    Singleton singleton() default Singleton.auto;

    /**
     * @return name of the InjectProvider
     */
    String provider() default "";
}

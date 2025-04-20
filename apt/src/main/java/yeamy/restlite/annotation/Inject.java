package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare to inject the field of {@link RESTfulResource} or a param of http method such as {@link GET}, {@link POST}...
 * <br>Only support param type: {@linkplain jakarta.servlet.ServletConfig ServletConfig},
 * {@linkplain jakarta.servlet.ServletContext ServletContext}<br>
 * <br>
 * <b>Search order: </b> {@link InjectProvider} -> only exist one of constructor, accessible static field,
 * accessible static method.
 * <pre>{@code
 * public class InjectType {
 *     // 1. accessible static field
 *     // public static final InjectType = new InjectType();
 *
 *     // 2. accessible static method
 *     // public static InjectType get(ServletConfig config) {
 *     //     return new InjectType();
 *     // }
 *
 *     // 3. accessible constructor
 *     public InjectType(ServletContext context) {
 *         // init here
 *     }
 * }
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Inject {

    /**
     * Specify which InjectProvider to create this http-body parameter
     *
     * @return name of the InjectProvider
     * @see InjectProvider
     */
    String provider() default "";
}

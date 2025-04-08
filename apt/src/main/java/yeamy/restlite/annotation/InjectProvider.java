package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the field/method/constructor are inject provider.<br>
 * Noted that the element must contain modifies public static.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface InjectProvider {
    /**
     * @return superclass of current class, keep empty if only for current class.
     */
    Class<?>[] provideFor() default {};

    /**
     * distinguish the constructor/static-method with same return type
     *
     * @return name of this provider
     */
    String value() default "";
}

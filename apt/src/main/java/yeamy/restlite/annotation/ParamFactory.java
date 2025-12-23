package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the type's default factory to generate instance of param.<br>
 * <pre>{@code
 * @Retention(RetentionPolicy.CLASS)
 * @Target(ElementType.PARAMETER)
 * @ParamFactory(processorClass = MaxTo15.class, processor = "maxTo15",
 *     nameMethod = "value", requiredMethod = "required")
 * public @interface MaxTo15Param {
 *     String value() default "";
 *     boolean required() default true;
 * }
 *
 * public class MaxTo15 {
 *     @PartProcessor("maxTo15")
 *     public static T myMethod(...) {
 *         ...
 *     }
 * }
 * }</pre>
 * @see Param
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ParamFactory {

    /**
     * Class name of static factory class.
     *
     * @return class of processor
     */
    Class<?> processorClass();

    /**
     * @return name of processor in the processorClass()
     */
    String processor();

    /**
     * @return name of the method in the annotation, which returns {@link Param#name()}
     */
    String nameMethod();

    /**
     * @return name of the method in the annotation, which returns {@link Param#required()}
     */
    String requiredMethod();
}

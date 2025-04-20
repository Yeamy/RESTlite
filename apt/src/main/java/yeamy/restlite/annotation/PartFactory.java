package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the annotation-type to replace @Part.<br>
 * <pre>{@code
 * @Retention(RetentionPolicy.CLASS)
 * @Target(ElementType.PARAMETER)
 * @PartFactory(processorClass = MyProcessorClass.class, processor = "myProcessName", nameMethod = "value")
 * public @interface MyProcessorPart {
 *     String value() default "";
 * }
 *
 * public class MyProcessorClass {
 *     @PartProcessor("myProcessName")
 *     public static T myMethod(...) {
 *         ...
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PartFactory {

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
     * @return name of the method in the annotation, which returns {@link Parts#name()}
     */
    String nameMethod();
}

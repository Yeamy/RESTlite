package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the annotation-type to replace @Body.<br>
 * <pre>{@code
 * @Retention(RetentionPolicy.CLASS)
 * @Target(ElementType.PARAMETER)
 * @BodyFactory(processorClass = MyProcessorClass.class, processor = "myProcessName")
 * public @interface MyProcessorBody {
 * }
 *
 * public class MyProcessorClass {
 *     @BodyProcessor("myProcessName")
 *     public static T myMethod(...) {
 *         ...
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BodyFactory {

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
}

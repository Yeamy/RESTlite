package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare the annotation-type to replace @Cookie.<br>
 * <pre>{@code
 * @Retention(RetentionPolicy.CLASS)
 * @Target(ElementType.PARAMETER)
 * @CookieFactory(processorClass = MyProcessorClass.class, processor = "myProcessName", nameMethod = "value")
 * public @interface MyCookie {
 *     String value() default "";
 * }
 *
 * public class MyProcessorClass {
 *     @CookieProcessor("myProcessName")
 *     public static T myMethod(...) {
 *         ...
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CookieFactory {

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
     * @return name of the method in the annotation, which returns {@link Cookies#name()}
     */
    String nameMethod();
}

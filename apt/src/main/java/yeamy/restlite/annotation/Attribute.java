package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http request attribute.<br>
 * <b>support type:</b> the given type or null
 *
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Param
 * @see Body
 * @see Parts
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Attribute {

    /**
     * @return name of Attribute, can be empty if same with parameter
     */
    String value() default "";
}

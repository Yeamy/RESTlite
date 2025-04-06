package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the parameter is http request header.<br>
 * <b>support type:</b> {@linkplain String String}, {@linkplain java.lang.Integer Integer}/int,
 * {@linkplain java.lang.Long Long}/long(timeMiles), {@linkplain java.util.Date Date},
 * type have public static-method/constructor with one param (param type as above),
 * type create by {@link HeaderProcessor},
 *
 * @author Yeamy
 * @see Cookies
 * @see Param
 * @see Body
 * @see Parts
 * @see Attribute
 * @see HeaderProcessor
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Header {
    /**
     * name of Header if same with parameter
     */
    String value() default "";

    /**
     * Distinguish the constructor/static-method with same return type
     *
     * @see HeaderProcessor
     */
    String processor() default "";
}

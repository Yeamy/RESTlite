package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare to inject the field of Resource.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Inject {

    /**
     * class name of creator.<br>
     * ├not empty:<br>
     * │ ├ with tag: use target.<br>
     * │ └ no tag:<br>
     * │&nbsp;&nbsp;├ lookup static method<br>
     * │&nbsp;&nbsp;└ use type's constructor.<br>
     * └empty:type's creator<br>
     * &nbsp;├ with tag: use target.<br>
     * &nbsp;└ no tag:<br>
     * &nbsp;&nbsp;├ lookup InjectProvider<br>
     * &nbsp;&nbsp;├ lookup static method<br>
     * &nbsp;&nbsp;└ use constructor<br>
     */
    String creator() default "";

    /**
     * tag the constructor/creator-method, suggest to use if more than one
     * constructor/method
     * @see LinkTag
     */
    String tag() default "";
}

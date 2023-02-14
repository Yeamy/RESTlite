package yeamy.restlite.annotation;

import java.lang.annotation.*;

/**
 * @see Body
 * @see Inject
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface LinkTag {

	/**
	 * tag the field/method/constructor
	 */
	String[] value();

}

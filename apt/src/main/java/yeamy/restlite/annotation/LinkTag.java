package yeamy.restlite.annotation;

import java.lang.annotation.*;

@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface LinkTag {

	/**
	 * tag the constructor and link the http-request-parameter, if more than one
	 * constructor
	 */
	String[] value() default {};

}

package yeamy.restlite.annotation;

import java.lang.annotation.*;

@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(LinkTags.class)
public @interface LinkTag {

	/**
	 * use for remark only, not effect for code
	 */
	Class<?>[] clz() default Object.class;

	/**
	 * tag the constructor and link the http-request-parameter, if more than one
	 * constructor
	 */
	String value() default "";

}

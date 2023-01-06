package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * support type : {@linkplain java.io.InputStream InputStream},
 * {@linkplain jakarta.servlet.ServletInputStream ServletInputStream}, byte[],
 * {@linkplain String String} or any declared type with annotation
 * {@link Body}
 * <br>
 * body can read only once per request
 * 
 * @author Yeamy
 * @see Header
 * @see Cookies
 * @see Param
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // ANNOTATION_TYPE need CLASS
public @interface Body {

	/**
	 * class name of static factory class. If empty, using type's Body annotation,
	 * if still empty, using type's constructor.
	 */
	String creator() default "";

	/**
	 * tag the constructor/creator-method, suggest to use if more than one
	 * constructor/method
	 * @see LinkTag
	 */
	String tag() default "";

	/**
	 * keep empty will be same as {@link Configuration}
	 */
	String charset() default "";
}

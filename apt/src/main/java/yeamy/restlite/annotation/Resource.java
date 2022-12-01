package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see javax.servlet.annotation.WebServlet
 * @see javax.servlet.annotation.MultipartConfig
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Resource {

//	Class<? extends RESTfulServlet> clz() default RESTfulServlet.class;

//	String encoding() default "utf-8";

//	String contentType() default "application/json";
	// @WebServlet ---------------------

	String value();

//	int loadOnStartup() default -1;

	boolean asyncSupported() default false;

//	String smallIcon() default "";

//	String largeIcon() default "";

//	String name() default "";

//	String description() default "";

//	String displayName() default "";

//	WebInitParam[] initParams() default {};

	// @MultipartConfig ---------------------
//	String location() default "";

	long maxFileSize() default -1L;

	long maxRequestSize() default -1L;

//	int fileSizeThreshold() default 0;
}

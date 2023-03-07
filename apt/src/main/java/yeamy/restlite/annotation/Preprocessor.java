package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Process the value before method invoke.
 * must be subtype of {@linkplain yeamy.restlite.addition.ValueProcessor ValueProcessor}
 * @author Yeamy
 * @see yeamy.restlite.addition.NoProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Preprocessor {
	String value();
}

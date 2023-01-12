package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * set the sequence of {@linkplain jakarta.servlet.annotation.WebListener WebListener}
 * or {@linkplain jakarta.servlet.annotation.WebFilter WebFilter}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Position {
    int value();
}

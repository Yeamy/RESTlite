package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods run before Tomcat. support argument:
 * {@linkplain org.apache.catalina.startup.Tomcat Tomcat},
 * {@linkplain org.apache.catalina.Context Context},
 * String[] or no param
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface RunBeforeTomcat {
}

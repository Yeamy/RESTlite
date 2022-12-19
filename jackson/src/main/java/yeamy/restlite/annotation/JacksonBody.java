package yeamy.restlite.annotation;

import yeamy.restlite.annotation.Creator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
@Creator(className = "yeamy.restlite.addition.JacksonParser")
public @interface JacksonBody {
}

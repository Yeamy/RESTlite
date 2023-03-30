package yeamy.restlite.annotation;

import yeamy.restlite.addition.JacksonParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
@Body(creator = JacksonParser.class, tag = "part")
public @interface JacksonPart {
}

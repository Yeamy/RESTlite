package yeamy.restlite.annotation;

import yeamy.restlite.addition.JacksonXmlParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
@Body(processor = JacksonXmlParser.class, tag = "body")
public @interface JacksonXmlBody {
}

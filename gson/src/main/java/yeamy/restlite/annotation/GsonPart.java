package yeamy.restlite.annotation;

import yeamy.restlite.addition.GsonParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
@Part(creator = GsonParser.class, tag = "part")
public @interface GsonPart {
}

package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
@BodyFactory(processorClass = JacksonXmlPart.class, processor = "jacksonXmlBody")
public @interface JacksonXmlBody {
}

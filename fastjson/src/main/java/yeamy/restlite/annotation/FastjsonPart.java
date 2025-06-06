package yeamy.restlite.annotation;

import yeamy.restlite.addition.FastjsonParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
@PartFactory(processorClass = FastjsonParser.class, processor = "fastjsonPart", nameMethod = "value")
public @interface FastjsonPart {

    String value() default "";

}

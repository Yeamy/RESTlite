package yeamy.restlite.annotation;

import yeamy.restlite.addition.FastjsonParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
@BodyFactory(processorClass = FastjsonParser.class, processor = "fastJsonBody")
public @interface FastjsonBody {
}

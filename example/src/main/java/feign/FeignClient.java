package feign;

import yeamy.restlite.annotation.Creator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Creator(className = "yeamy.restlite.example.feign.FeignImpl")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeignClient {

	String baseUrl();
}

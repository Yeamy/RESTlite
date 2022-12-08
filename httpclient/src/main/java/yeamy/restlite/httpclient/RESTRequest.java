package yeamy.restlite.httpclient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface RESTRequest {

    Protocol protocol() default Protocol.HTTP_1_1;

    String method() default "GET";

    String host() default "";

    String resource() default "";

    String charset() default "UTF-8";

    String contentType() default "application/json";

    Values[] header() default {};

    Values[] cookie() default {};

    Values[] param() default {};
}

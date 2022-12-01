package yeamy.restlite.annotation;

import yeamy.restlite.RESTfulRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate type with its creator(factory) class name and method tag;
 *
 * <pre>
 * &#64;GET
 * public String method(&#64;Extra A a){...}
 *
 * &#64;Creator(className = "demo.GsonParser", tag = "json")
 * public class A {
 * 	...
 * }
 * </pre>
 * If annotate for {@link Extra}, support parameter type: {@link Header},
 * {@link Cookies}, {@link Param}, any other {@link Extra},
 * {@linkplain RESTfulRequest RESTfulRequest},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 * Class&lt;T&gt;
 * <pre>
 *
 * public class JsonParser {
 *   public static A parse(HttpServletRequest _req) {
 *     ...
 *     return new A();
 *   }
 * }
 * </pre>
 * If annotate for {@link Body}, support parameter type: {@link Header},
 * {@link Cookies}, {@link Param}, {@link Extra},
 * {@linkplain java.io.InputStream InputStream},
 * {@linkplain jakarta.servlet.ServletInputStream ServletInputStream},
 * {@linkplain RESTfulRequest RESTfulRequest},
 * {@linkplain jakarta.servlet.http.HttpServletRequest HttpServletRequest},
 * Class&lt;T&gt;
 *
 * <pre>
 * // method in {@linkplain yeamy.restlite.annotation.Resource Resource}
 * &#64;GET
 * public String method(&#64;Body A a){...}
 *
 * // method in Creator
 * &#64;LinkTag("json")
 * public static &lt;T&gt; T parse(String contentType, ServletInputStream is, Class&lt;T&gt; clz) {
 *   return new Gson().fromJson(StreamUtils.readString(is, "UTF-8"), clz);
 * }
 * </pre>
 *
 * @author Yeamy
 * @see Body
 * @see Extra
 * @see LinkTag
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface Creator {

    /**
     * class name of static factory class, if empty using itself
     */
    String className() default "";

    /**
     * method name to creator
     */
    String tag() default "";

}

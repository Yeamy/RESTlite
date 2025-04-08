package yeamy.restlite.annotation;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the REST resource
 *
 * @see WebServlet
 * @see MultipartConfig
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RESTfulResource {

    // @WebServlet ---------------------

    /**
     * for example:
     * <pre>{@code
     *     url:      http://abc.com/house/1/room/2
     *     resource: house, room
     * }</pre>
     * @return resource of REST-API
     */
    String value();

    /**
     * @return array of initialization params for this Servlet
     *
     * @see WebServlet#initParams()
     */
    WebInitParam[] initParams() default {};

    // @MultipartConfig ---------------------

    /**
     * @return location in which the Container stores temporary files
     *
     * @see MultipartConfig#location()
     */
    String tempLocation() default "";

    /**
     * @return the maximum size of the request allowed for multipart/form-data
     *
     * @see MultipartConfig#maxRequestSize()
     */
    long maxRequestSize() default -1L;

    /**
     * @return the maximum size allowed for uploaded files (in bytes)
     *
     * @see MultipartConfig#maxFileSize()
     */
    long maxFileSize() default -1L;

    /**
     * @return the size threshold at which the file will be written to the disk
     *
     * @see MultipartConfig#fileSizeThreshold()
     */
    int fileSizeThreshold() default 0;
}

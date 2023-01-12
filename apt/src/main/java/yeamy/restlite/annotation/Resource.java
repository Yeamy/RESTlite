package yeamy.restlite.annotation;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the REST Resource
 *
 * @see WebServlet
 * @see MultipartConfig
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Resource {

    // @WebServlet ---------------------

    /**
     * name of Resource
     */
    String value();

    /**
     * array of initialization params for this Servlet
     *
     * @see WebServlet#initParams()
     */
    WebInitParam[] initParams() default {};

    // @MultipartConfig ---------------------

    /**
     * location in which the Container stores temporary files
     *
     * @see MultipartConfig#location()
     */
    String tempLocation() default "";

    /**
     * the maximum size of the request allowed for multipart/form-data
     *
     * @see MultipartConfig#maxRequestSize()
     */
    long maxRequestSize() default -1L;

    /**
     * the maximum size allowed for uploaded files (in bytes)
     *
     * @see MultipartConfig#maxFileSize()
     */
    long maxFileSize() default -1L;

    /**
     * the size threshold at which the file will be written to the disk
     *
     * @see MultipartConfig#fileSizeThreshold()
     */
    int fileSizeThreshold() default 0;
}

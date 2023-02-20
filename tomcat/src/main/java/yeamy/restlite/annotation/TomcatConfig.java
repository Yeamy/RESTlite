package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tomcat Config
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface TomcatConfig {

    /**
     * qualified name of main class,
     * default "Main" with same package of the class with annotation @TomcatConfig
     */
    String main() default "";

    /**
     * keep empty for the “user.dir” system property (the directory where Java was run from)
     *
     * @see org.apache.catalina.startup.Tomcat#setBaseDir(String)
     */
    String baseDir() default "";

    /**
     * Maximum amount of worker threads.
     */
    int maxThreads() default 0;

    /**
     * Minimum amount of worker threads. if not set, default value is 10
     */
    int minSpareThreads() default 0;

    /**
     * When Tomcat expects data from the client, this is the time Tomcat will
     * wait for that data to arrive before closing the connection.
     */
    int connectionTimeout() default 0;

    /**
     * Maximum number of connections that the server will accept and process at any
     * given time. Once the limit has been reached, the operating system may still
     * accept connections based on the "acceptCount" property.
     */
    int maxConnections() default 0;

    /**
     * Maximum queue length for incoming connection requests when all possible request
     * processing threads are in use.
     */
    int acceptCount() default 0;

    Connector[] connector();
}

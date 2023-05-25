package yeamy.restlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * declare the interface is nacos client<br>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface NacosRemoteServer {

    /**
     * The property of "endpoint".
     *
     * @return empty as default value
     */
    String endpoint() default "";

    /**
     * The property of "namespace".
     *
     * @return empty as default value
     */
    String namespace() default "";

    /**
     * The property of "access-key".
     *
     * @return empty as default value
     */
    String accessKey() default "";

    /**
     * The property of "secret-key".
     *
     * @return empty as default value
     */
    String secretKey() default "";

    /**
     * The property of "server-addr".
     *
     * @return empty as default value
     */
    String serverAddr() default "";

    /**
     * The property of "context-path".
     *
     * @return empty as default value
     */
    String contextPath() default "";

    /**
     * The property of "cluster-name".
     *
     * @return empty as default value
     */
    String clusterName() default "";

    /**
     * The property of "encode".
     *
     * @return "UTF-8" as default value
     */
    String encode() default "";

    /**
     * The property of "configLongPollTimeout".
     *
     * @return empty as default value
     */
    String configLongPollTimeout() default "";

    /**
     * The property of "configRetryTime".
     *
     * @return empty as default value
     */
    String configRetryTime() default "";

    /**
     * The property of "maxRetry".
     *
     * @return empty as default value
     */
    String maxRetry() default "";

    /**
     * The property of "enableRemoteSyncConfig".
     *
     * @return empty as default value
     */
    String enableRemoteSyncConfig() default "";

    /**
     * The property of "username".
     *
     * @return empty as default value
     */
    String username() default "";

    /**
     * The property of "password".
     *
     * @return empty as default value
     */
    String password() default "";

    NacosDiscovery[] enableDiscovery() default {};
}

package yeamy.restlite.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see NacosRemoteServer
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface NacosDiscovery {

    String serviceName();

    String serviceIP() default "localhost";

    int servicePort();

    String clusterName() default "DEFAULT";
}

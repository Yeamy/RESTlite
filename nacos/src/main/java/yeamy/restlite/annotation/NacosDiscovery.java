package yeamy.restlite.annotation;

public @interface NacosDiscovery {

    String serviceName();

    String serviceIP() default "localhost";

    int servicePort();

    String clusterName() default "DEFAULT";
}

package yeamy.restlite.annotation;

/**
 * Tomcat Connector Config
 */
public @interface Connector {

    /**
     * Set the port for the connector.
     */
    int port();

    /**
     * Set the name of the host, "" for default value 'localhost'.
     */
    String hostName() default "";

    /**
     * Set the redirect port number. (non-SSL to SSL)
     */
    int redirectPort() default 0;

    /**
     * Set the secure connection flag that will be assigned to requests
     * received through this connector.
     */
    boolean secure() default false;

    /**
     * e.g. TLSv1.1+TLSv1.2+TLSv1.3
     *
     * @see org.apache.tomcat.util.net.Constants
     */
    String sslProtocol() default "TSL";

    /**
     * Set the new cipher configuration. Note: Regardless of the format used to
     * set the configuration, it is always stored in OpenSSL format.
     *
     * @return The new cipher configuration in OpenSSL or JSSE format
     */
    String ciphers() default "";

    /**
     * keystore type e.g. PKCS12
     */
    String keyStoreType() default "";

    /**
     * keystore file path, if in current jar file withing prefix "!":<br>
     * <b>!a/b/c/keystore.pfx</b> // keystore in package: a.b.c<br>
     * <b>/home/keystore.pfx</b> // keystore in local storage: /home
     */
    String keyStoreFile() default "";

    /**
     * keystore password
     */
    String keyStorePass() default "";
}

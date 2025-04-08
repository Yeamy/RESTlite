package yeamy.restlite.annotation;

/**
 * Tomcat Connector Config
 */
public @interface Connector {

    /**
     * @return Network port number of the connector.
     */
    int port();

    /**
     * @return name of the host, "" for default value 'localhost'.
     */
    String hostName() default "";

    /**
     * @return redirect port number. (non-SSL to SSL)
     */
    int redirectPort() default 0;

    /**
     * turn on secure options, include sslProtocol(), ciphers(), keyStoreType(), keyStoreFile(), keyStorePass()
     *
     * @return turn on or not
     */
    boolean secure() default false;

    /**
     * ssl protocol type e.g. TLSv1.1+TLSv1.2+TLSv1.3
     *
     * @return ssl protocol type
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
     * @return keystore type e.g. PKCS12
     */
    String keyStoreType() default "";

    /**
     * keystore file path, if in current jar file withing prefix "!":<br>
     * <b>!a/b/c/keystore.pfx</b> // keystore in package: a.b.c<br>
     * <b>/home/keystore.pfx</b> // keystore in local storage: /home
     *
     * @return keystore file path
     */
    String keyStoreFile() default "";

    /**
     * @return keystore password
     */
    String keyStorePass() default "";
}

package yeamy.restlite.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;

public class TomcatUtils {

    private static int getInt(Properties properties, String key, int defaultValue) {
        if (properties.containsKey(key)) {
            String v = properties.getProperty(key);
            try {
                return Integer.parseInt(v);
            } catch (Exception ignored) {
            }
        }
        return defaultValue;
    }

    private static boolean getBool(Properties properties, String key) {
        if (properties.containsKey(key)) {
            String v = properties.getProperty(key);
            try {
                return Boolean.parseBoolean(v);
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static Context addContext(Properties properties, Tomcat tomcat) {
        if (properties.containsKey("hostName")) {
            tomcat.setHostname(properties.getProperty("hostName", ""));
        }
        if (properties.containsKey("minSpareThreads")
                || properties.containsKey("maxThreads")
                || properties.containsKey("connectionTimeout")
                || properties.containsKey("maxConnections")
                || properties.containsKey("acceptCount")) {
            ProtocolHandler handler = tomcat.getConnector().getProtocolHandler();
            if (handler instanceof AbstractProtocol<?> protocol) {
                int minSpareThreads = getInt(properties, "minSpareThreads", 0);
                if (minSpareThreads > 0) {
                    protocol.setMinSpareThreads(minSpareThreads);
                }
                int maxThreads = getInt(properties, "maxThreads", 0);
                if (maxThreads > 0) {
                    protocol.setMaxThreads(maxThreads);
                }
                int connectionTimeout = getInt(properties, "connectionTimeout", 0);
                if (maxThreads > 0) {
                    protocol.setConnectionTimeout(connectionTimeout);
                }
                int maxConnections = getInt(properties, "maxConnections", 0);
                if (maxThreads > 0) {
                    protocol.setMaxConnections(maxConnections);
                }
                int acceptCount = getInt(properties, "acceptCount", 0);
                if (maxThreads > 0) {
                    protocol.setAcceptCount(acceptCount);
                }
                int keepAliveTimeout = getInt(properties, "keepAliveTimeout", 0);
                if (maxThreads > 0) {
                    protocol.setKeepAliveTimeout(keepAliveTimeout);
                }
            }
        }
        Host host = tomcat.getHost();
        String baseDir = properties.getProperty("baseDir", "");
        if (!baseDir.isEmpty()) {
            tomcat.setBaseDir(baseDir);
        }
        String contextDir = properties.getProperty("contextDir", "");
        if (contextDir.isEmpty()) {
            contextDir = null;
        }
        return tomcat.addContext(host, "", contextDir);
    }

    public static void addConnectors(Properties properties, Tomcat tomcat) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String prefix = "connector" + i + ".";
            if (properties.containsKey(prefix + "port")) {
                addConnector(properties, prefix, tomcat);
            } else {
                break;
            }
        }
    }

    private static void addConnector(Properties properties, String prefix, Tomcat tomcat) {
        Connector conn = new Connector();
        conn.setURIEncoding("UTF-8");
        boolean secure = getBool(properties, prefix + "secure");
        conn.setPort(getInt(properties, prefix + "port", secure ? 443 : 80));//

        int redirectPort = getInt(properties, prefix + "redirectPort", 0);
        if (redirectPort > 0) {
            conn.setRedirectPort(redirectPort);//
        }
        if (secure) {
            KeyStore keyStore = getKeyStore(properties.getProperty(prefix + "keyStoreFile"),
                    properties.getProperty(prefix + "keyStorePass"),
                    properties.getProperty(prefix + "keyStoreType"));
            if (keyStore != null) {
                conn.setSecure(true);
                SSLHostConfig ssl = new SSLHostConfig();
                ssl.setCertificateVerification("required");
                ssl.setHostName(properties.getProperty(prefix + "hostName"));
                ssl.setSslProtocol(properties.getProperty(prefix + "sslProtocol"));
                ssl.setCiphers(properties.getProperty(prefix + "ciphers"));
                SSLHostConfigCertificate cert = new SSLHostConfigCertificate();
                cert.setCertificateKeystore(keyStore);
                ssl.addCertificate(cert);
                conn.addSslHostConfig(ssl);
            }
        }
        tomcat.getService().addConnector(conn);
    }

    public static KeyStore getKeyStore(String file, String pwd, String type) {
        try (InputStream is = (file.charAt(0) == '!')
                ? TomcatUtils.class.getClassLoader().getResourceAsStream(file.substring(1))
                : new FileInputStream(file)) {
            KeyStore ks = KeyStore.getInstance(type);
            ks.load(is, pwd.toCharArray());
            return ks;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Properties getProperties(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-tomcat".equals(arg)) {
                if (i + 1 < args.length) {
                    File f = new File(args[i + 1]);
                    if (f.exists()) {
                        try {
                            Properties p = new Properties();
                            p.load(new FileInputStream(f));
                            return p;
                        } catch (Exception ignored) {
                        }
                    }
                }
                System.out.println("Cannot find properties file, path is empty!");
            }
        }
        return null;
    }
}

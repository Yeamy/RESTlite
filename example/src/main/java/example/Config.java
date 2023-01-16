package example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yeamy.restlite.annotation.Configuration;
import yeamy.restlite.annotation.Connector;
import yeamy.restlite.annotation.SupportPatch;
import yeamy.restlite.annotation.TomcatConfig;

@TomcatConfig(connector = {
        @Connector(port = 80,
        redirectPort = 443),
        @Connector(port = 443,
        secure = true,
        keyStoreType = "PKCS12",
        keyStoreFile = "!a.pfx",
        keyStorePass = "1234")})
@Configuration(response = "example.MyResponse",
        supportPatch = SupportPatch.tomcat)
public class Config {
    public static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss Z").create();
}

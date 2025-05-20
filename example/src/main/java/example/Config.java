package example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yeamy.restlite.HttpResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.annotation.Configuration;
import yeamy.restlite.annotation.Connector;
import yeamy.restlite.annotation.PermissionHandle;
import yeamy.restlite.annotation.TomcatConfig;

@TomcatConfig(connector = {
        @Connector(port = 80,
        redirectPort = 443),
        @Connector(port = 443,
        secure = true,
        keyStoreType = "PKCS12",
        keyStoreFile = "!a.pfx",
        keyStorePass = "1234")})
@Configuration(response = MyResponse.class)
public class Config {
    public static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @PermissionHandle
    public static HttpResponse handlePermission(String permission, RESTfulRequest request) {
        return null;
    }
}

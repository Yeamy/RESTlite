package yeamy.restlite.httpclient;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpClientImpl {
    private static final CloseableHttpClient client = HttpClients.createDefault();

    public static <T> T execute(ClientRequest bean, HttpClientResponseHandler<T> handler) {
        try {
            return client.execute(bean.create(), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Charset charset(ClassicHttpResponse response) throws ProtocolException {
        Header contentType = response.getHeader("Content-Type");
        if (contentType != null) {
            String[] parts = contentType.getValue().split(";");
            if (parts.length > 1) {
                String[] charsetPart = parts[1].split("=");
                if (charsetPart.length == 2 && "charset".equalsIgnoreCase(charsetPart[0].trim())) {
                    return Charset.forName(charsetPart[1]);
                }
            }
        }
        return UTF_8;
    }

}

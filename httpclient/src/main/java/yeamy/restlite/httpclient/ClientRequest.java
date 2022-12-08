package yeamy.restlite.httpclient;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import yeamy.utils.TextUtils;

import java.net.URI;
import java.util.HashMap;

public class ClientRequest {
    private final HashMap<String, Object> header = new HashMap<>();
    private final HashMap<String, String> cookie = new HashMap<>();
    private final HashMap<String, String> param = new HashMap<>();

    public ProtocolVersion protocol = HttpVersion.HTTP_1_1;
    public String method = "GET";

    public String baseUri = "";

    public String resource = "";

    public String charset = "UTF-8";

    public String contentType = "application/json";

    ClientRequest addHeader(String k, String v) {
        header.put(k, v);
        return this;
    }

    ClientRequest addHeader(String k, Number v) {
        header.put(k, String.valueOf(v));
        return this;
    }

    public HashMap<String, Object> getHeader() {
        return header;
    }

    ClientRequest addCookie(String k, String v) {
        cookie.put(k, v);
        return this;
    }

    public HashMap<String, String> getCookie() {
        return cookie;
    }

    ClientRequest addParam(String k, String v) {
        param.put(k, v);
        return this;
    }

    public HashMap<String, String> getParam() {
        return param;
    }

    public URI uri() {
        StringBuilder uri = new StringBuilder(baseUri);
        if (TextUtils.isEmpty(resource)) {
            uri.append('?');
            if (param.size() > 0) {
                param.forEach((k, v) -> uri.append(k).append('=').append(v).append('&'));
                uri.deleteCharAt(uri.length() - 1);
            }
        } else {
            param.forEach((k, v) -> uri.append('/').append(k).append('/').append(v));
            uri.append('/').append(resource);
        }
        return URI.create(uri.toString());
    }

    public ClassicHttpRequest create() {
        HttpUriRequestBase req = new HttpUriRequestBase(method, uri());
        header.forEach(req::setHeader);
        if (cookie.size() > 0) {
            BasicCookieStore store = new BasicCookieStore();
            cookie.forEach((k, v) -> store.addCookie(new BasicClientCookie(k, v)));
            req.setHeader("Cookie", store);
        }
        req.setVersion(protocol);
        return req;
    }
}

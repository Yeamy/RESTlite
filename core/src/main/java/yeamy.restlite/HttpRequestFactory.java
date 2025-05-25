package yeamy.restlite;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import yeamy.restlite.utils.StreamUtils;
import yeamy.restlite.utils.TextUtils;

import java.io.IOException;
import java.util.Collection;

public class HttpRequestFactory {

    public static RESTfulRequest createRequest(HttpServletRequest req) throws ServletException, IOException {
        RESTfulRequest out = new RESTfulRequest();
        out.insert(req);
        readUri(req, out);
        readBody(req, out);
        return out;
    }

    private static void readUri(HttpServletRequest req, RESTfulRequest out) {
        String reqUri = req.getRequestURI();
        String baseUrl = req.getServletContext().getContextPath();
        int begin = baseUrl.length();
        if (reqUri.charAt(begin) == '/') ++begin;
        int end = reqUri.length();
        if (reqUri.charAt(end - 1) == '/') --end;
        String uri = reqUri.substring(begin, end);
        if (TextUtils.isEmpty(uri)) {
            return;
        }
        String[] kv = uri.split("/");
        int length = kv.length;
        if ((length) % 2 == 1) {
            out.setResource(kv[length - 1]);
        } else {
            out.setResource(kv[length - 2]);
        }
        for (int i = 0; i < length; i += 2) {
            if (length > i + 1) {
                out.addParameter(kv[i], kv[i + 1]);
            }
        }
        out.dispatch = !out.getResource().equals(kv[0]);
    }

    public static void readBody(HttpServletRequest req, RESTfulRequest out) throws ServletException, IOException {
        if (TextUtils.in(req.getMethod(), "GET", "HEAD", "OPTIONS")) {
            return;
        }
        String contentType = req.getContentType();
        if (TextUtils.isEmpty(contentType)) {
            return;
        }
        contentType = contentType.toLowerCase();
        if (contentType.contains("multipart/form-data")) {
            readMultiPart(req, out);
        }
    }

    private static void readMultiPart(HttpServletRequest req, RESTfulRequest out) throws ServletException, IOException {
        Collection<Part> parts = req.getParts();
        for (Part part : parts) {
            String name = part.getName();
            String contentType = part.getContentType();
            if (contentType == null) {
                String value = StreamUtils.readString(part.getInputStream(), out.getCharset());
                out.addParameter(name, value);
            } else {
                out.addFile(name, new HttpRequestFile(part, out.getCharset()));
            }
        }
    }

}

package yeamy.restlite.addition;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import yeamy.restlite.HttpResponse;

/**
 * @see ExceptionResponse
 * @see StreamResponse
 * @see TextPlainResponse
 */
public abstract class AbstractHttpResponse<T> implements HttpResponse {
    private transient int status = HttpServletResponse.SC_OK;
    private transient String mime = null;
    private transient String charset = "UTF-8";
    private transient Collection<Cookie> cookies;
    private final transient LinkedHashMap<String, Object> headers = new LinkedHashMap<>();

    private transient T data;

    public AbstractHttpResponse(T data) {
        this.data = data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setStatus(int code) {
        this.status = code;
    }

    public int status() {
        return status;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    public void setContentType(String mime) {
        this.mime = mime;
    }

    public String contentType() {
        return mime;
    }

    public void setIntHeader(String k, int v) {
        headers.put(k, v);
    }

    public void setDateHeader(String k, Date v) {
        headers.put(k, v.getTime());
    }

    public void setDateHeader(String k, long v) {
        headers.put(k, v);
    }

    public void setHeader(String k, String v) {
        headers.put(k, v);
    }

    public void addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        cookies.add(cookie);
    }

    public Collection<Cookie> cookies() {
        return cookies;
    }

    @Override
    public void write(HttpServletResponse resp) throws IOException {
        resp.setStatus(status);
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                resp.setIntHeader(entry.getKey(), (Integer) value);
            } else if (value instanceof Long) {
                resp.setDateHeader(entry.getKey(), (Long) value);
            } else {
                resp.setHeader(entry.getKey(), value.toString());
            }
        }
        resp.setCharacterEncoding(charset);
        if (mime != null) {
            resp.setContentType(mime);
        }
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                resp.addCookie(cookie);
            }
        }
        writeContent(resp);
    }

    protected abstract void writeContent(HttpServletResponse resp) throws IOException;
}

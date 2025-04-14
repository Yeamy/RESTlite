package yeamy.restlite.addition;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.HttpResponse;

import java.io.IOException;
import java.util.*;

/**
 * @see ExceptionResponse
 * @see StreamResponse
 * @see TextPlainResponse
 */
public abstract class AbstractHttpResponse<T> implements HttpResponse {
    private transient int status;
    private transient String mime = null;
    private transient String charset = "UTF-8";
    private transient Collection<Cookie> cookies;
    private final transient LinkedHashMap<String, Object> headers = new LinkedHashMap<>();

    private transient T data;

    /**
     * response with default http status code 200
     *
     * @param data data serialize to response body
     */
    public AbstractHttpResponse(T data) {
        this(HttpServletResponse.SC_OK, data);
    }

    /**
     * @param status http status code
     * @param data   data serialize to response body
     */
    public AbstractHttpResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    /**
     * @param data data serialize to response body
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * @return data serialize to response body
     */
    public T getData() {
        return data;
    }

    /**
     * @param code http status code
     */
    public void setStatus(int code) {
        this.status = code;
    }

    /**
     * @return http status code
     */
    public int status() {
        return status;
    }

    /**
     * @param charset set the content charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * @return get content charset default is "UTF-8"
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @param mime set the content mime type
     */
    public void setContentType(String mime) {
        this.mime = mime;
    }

    /**
     * @return response content type, such as "text/plain"
     */
    public String contentType() {
        return mime;
    }

    /**
     * @param k header name
     * @param v header value
     */
    public void setIntHeader(String k, int v) {
        headers.put(k, v);
    }

    /**
     * @param k header name
     * @param v header value
     */
    public void setDateHeader(String k, Date v) {
        headers.put(k, v.getTime());
    }

    /**
     * @param k header name
     * @param v header value
     */
    public void setDateHeader(String k, long v) {
        headers.put(k, v);
    }

    /**
     * @param k header name
     * @param v header value
     */
    public void setHeader(String k, String v) {
        headers.put(k, v);
    }

    /**
     * @param cookie response cookie
     */
    public void addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        cookies.add(cookie);
    }

    /**
     * @return get all cookies
     */
    public Collection<Cookie> cookies() {
        return cookies;
    }

    @Override
    public void write(HttpServletResponse resp) throws IOException {
        resp.setStatus(status);
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer intVal) {
                resp.setIntHeader(entry.getKey(), intVal);
            } else if (value instanceof Long longVal) {
                resp.setDateHeader(entry.getKey(), longVal);
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

    /**
     * Serialize output stream
     *
     * @param resp the http servlet response
     * @throws IOException If an input or output exception occurs
     * @see #write
     */
    protected abstract void writeContent(HttpServletResponse resp) throws IOException;
}

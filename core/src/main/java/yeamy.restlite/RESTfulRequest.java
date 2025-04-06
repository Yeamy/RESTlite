package yeamy.restlite;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import yeamy.utils.SingletonPool;
import yeamy.utils.StreamUtils;
import yeamy.utils.TextUtils;
import yeamy.utils.ValueUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class RESTfulRequest implements Serializable {
    private static final HttpRequestFile[] NO_FILE = new HttpRequestFile[0];
    private static final Cookie[] NO_COOKIE = new Cookie[0];

    public static final String REQUEST = "RESTlite:Request";

    public static RESTfulRequest get(ServletRequest r) {
        return (RESTfulRequest) r.getAttribute(REQUEST);
    }

    @Serial
    private static final long serialVersionUID = -7894023380274904092L;
    private HttpServletRequest req;
    private String resource = "", serviceName;
    private final HashMap<String, String> parameter = new HashMap<>();
    private HashMap<String, HttpRequestFile> files;
    private HashSet<String> accept;
    boolean dispatch = false;

    private Cookie[] cookies;

    RESTfulRequest getForward(String resource) {
        RESTfulRequest r = new RESTfulRequest();
        r.req = req;
        r.resource = resource;
        r.parameter.putAll(parameter);
        if (files != null) {
            r.files = new HashMap<>(files);
        }
        return r;
    }

    void insert(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletRequest getRequest() {
        return req;
    }

    public String getRemoteAddr() {
        return req.getRemoteAddr();
    }

    public int getRemotePort() {
        return req.getRemotePort();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    void setResource(String resource) {
        this.resource = resource;
    }

    void addFile(String name, HttpRequestFile file) {
        if (files == null) {
            files = new HashMap<>();
        }
        this.files.put(name, file);
    }

    public String getHeader(String name) {
        return req.getHeader(name);
    }

    public String[] getHeaderAsArray(String name) {
        String header = req.getHeader(name);
        if (header == null) return SingletonPool.EMPTY_STRING_ARRAY;
        String[] array = header.split(",");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    public List<String> getHeaders(String name) {
        return Collections.list(req.getHeaders(name));
    }

    public long getDateHeader(String name) {
        return req.getDateHeader(name);
    }

    public int getIntHeader(String name) {
        return req.getIntHeader(name);
    }

    public Enumeration<String> getHeaderNames() {
        return req.getHeaderNames();
    }

    public Object getAttribute(String name) {
        return req.getAttribute(name);
    }

    public void setAttribute(String name, Object object) {
        req.setAttribute(name, object);
    }

    /**
     * get http header "Accept"
     */
    public Set<String> getAccept() {
        if (accept == null) {
            accept = new HashSet<>();
            String raw = req.getHeader("Accept");
            if (TextUtils.isNotEmpty(raw)) {
                String[] ss = raw.split(",");
                Collections.addAll(accept, ss);
            }
        }
        return accept;
    }

    /**
     * @return if http header "Accept" contains mime
     */
    public boolean isAccept(String mime) {
        Set<String> set = getAccept();
        if (set.contains(mime)) {
            return true;
        } else {
            for (String m : set) {
                if (m.contains(mime)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addParameter(String name, String value) {
        parameter.put(name, value);
    }

    public void addParameter(Map<String, String> map) {
        Set<Entry<String, String>> set = map.entrySet();
        for (Entry<String, String> entry : set) {
            addParameter(entry.getKey(), entry.getValue());
        }
    }

    public HttpRequestFile[] getFiles() {
        return files == null
                ? NO_FILE
                : files.values().toArray(HttpRequestFile[]::new);
    }

    public HttpRequestFile getFile(String name) {
        return files == null ? null : files.get(name);
    }

    public Part getPart(String name) throws ServletException, IOException {
        return req.getPart(name);
    }

    public Part[] getParts() throws ServletException, IOException {
        return req.getParts().toArray(Part[]::new);
    }

    public String getContentType() {
        return req.getContentType();
    }

    public ServletInputStream getBody() throws IOException {
        return req.getInputStream();
    }

    public byte[] getBodyAsByte() {
        try (InputStream is = req.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream(is.available())) {
            StreamUtils.writeWithoutClose(os, is);
            return os.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getCharset() {
        return req.getCharacterEncoding();
    }

    public String getBodyAsText() throws IOException {
        Charset charset;
        try {
            charset = Charset.forName(getCharset());
        } catch (Exception e) {
            charset = StandardCharsets.UTF_8;
        }
        return getBodyAsText(charset);
    }

    public String getBodyAsText(String charset) throws IOException {
        return getBodyAsText(Charset.forName(charset));
    }

    public String getBodyAsText(Charset charset) throws IOException {
        try (InputStream is = req.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream(is.available())) {
            StreamUtils.writeWithoutClose(os, is);
            return os.toString(charset);
        }
    }

    public String getMethod() {
        return req.getMethod();
    }

    public String getResource() {
        return resource;
    }

    public boolean has(String name) {
        return parameter.containsKey(name) || req.getParameter(name) != null;
    }

    public Map<String, String> getParams() {
        Map<String, String> out = new HashMap<>();
        Map<String, String[]> map = req.getParameterMap();
        Set<Entry<String, String[]>> set = map.entrySet();
        for (Entry<String, String[]> entry : set) {
            out.put(entry.getKey(), entry.getValue()[0]);
        }
        out.putAll(parameter);
        return out;
    }

    public String[] getParams(String name) {
        String value = this.parameter.get(name);
        String[] array = req.getParameterValues(name);
        if (value == null) {
            return array;
        } else {
            if (array == null) {
                return new String[]{value};
            } else {
                String[] r = new String[array.length + 1];
                r[0] = value;
                System.arraycopy(array, 0, r, 1, array.length);
                return r;
            }
        }
    }

    public Integer[] getIntegerParams(String name) {
        return ValueUtils.allToInteger(getParams(name));
    }

    public Long[] getLongParams(String name) {
        return ValueUtils.allToLong(getParams(name));
    }

    public Float[] getFloatParams(String name) {
        return ValueUtils.allToFloat(getParams(name));
    }

    public Double[] getDoubleParams(String name) {
        return ValueUtils.allToDouble(getParams(name));
    }

    public Boolean[] getBooleanParams(String name) {
        return ValueUtils.allToBoolean(getParams(name));
    }

    public BigDecimal[] getDecimalParams(String name) {
        return ValueUtils.allToBigDecimal(getParams(name));
    }

    public String getParam(String name, String fallback) {
        String param = getParam(name);
        return param == null ? fallback : param;
    }

    /**
     * @param name name of param
     * @return value of param, if fail return null
     */
    public String getParam(String name) {
        String value = this.parameter.get(name);
        if (value != null) {
            return value;
        }
        return req.getParameter(name);
    }

    /**
     * @param name name of param
     * @return BigDecimal type value, if fail return null
     */
    public BigDecimal getDecimalParam(String name) {
        return ValueUtils.toBigDecimal(getParam(name));
    }

    public BigDecimal getDecimalParam(String name, BigDecimal fallback) {
        BigDecimal param = ValueUtils.toBigDecimal(getParam(name));
        return param != null ? param : fallback;
    }

    public boolean getBooleanParam(String name, boolean fallback) {
        return ValueUtils.toBoolean(getParam(name), fallback);
    }

    /**
     * @param name name of param
     * @return Boolean type value, if fail return null
     */
    public Boolean getBooleanParam(String name) {
        return ValueUtils.toBoolean(getParam(name));
    }

    public int getIntParam(String name, int fallback) {
        return ValueUtils.toInt(getParam(name), fallback);
    }

    /**
     * @param name name of param
     * @return Integer type value, if fail return null
     */
    public Integer getIntegerParam(String name) {
        return ValueUtils.toInteger(getParam(name));
    }

    /**
     * @param name name of param
     * @return Long type value, if fail return null
     */
    public Long getLongParam(String name) {
        return ValueUtils.toLong(getParam(name));
    }

    public long getLongParam(String name, long fallback) {
        return ValueUtils.toLong(getParam(name), fallback);
    }

    public Float getFloatParam(String name) {
        return ValueUtils.toFloat(getParam(name));
    }

    public float getFloatParam(String name, float fallback) {
        return ValueUtils.toFloat(getParam(name), fallback);
    }

    public Double getDoubleParam(String name) {
        return ValueUtils.toDouble(getParam(name));
    }

    public double getDoubleParam(String name, double fallback) {
        return ValueUtils.toDouble(getParam(name), fallback);
    }

    /**
     * @return parameter value as int of current Resource
     */
    public Integer getIntValue() {
        return getIntegerParam(resource);
    }

    /**
     * @return parameter value of current Resource
     */
    public String getValue() {
        return getParam(resource);
    }

    public String getPathParam(String name) {
        return this.parameter.get(name);
    }

    public String getQueryParam(String name) {
        return req.getParameter(name);
    }

    public Cookie[] getCookies() {
        if (cookies == null) {
            cookies = req.getCookies();
            if (cookies == null) {
                cookies = NO_COOKIE;
            }
        }
        return cookies;
    }

    public Cookie getCookie(String name) {
        Cookie[] cookies = getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public String getCookieValue(String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}

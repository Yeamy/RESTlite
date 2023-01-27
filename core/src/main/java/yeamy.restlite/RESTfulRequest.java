package yeamy.restlite;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import yeamy.utils.StreamUtils;
import yeamy.utils.TextUtils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class RESTfulRequest implements Serializable {
    public static final String REQUEST = "RESTlite:Request";

    public static RESTfulRequest get(ServletRequest r) {
        return (RESTfulRequest) r.getAttribute(REQUEST);
    }

    private static final long serialVersionUID = -7894023380274904092L;
    private HttpServletRequest req;
    private String resource = "", serviceName;
    private final HashMap<String, String> parameter = new HashMap<>();
    private HashMap<String, HttpRequestFile> fields;
    private HashSet<String> accept;
    boolean dispatch = false;

    private Cookie[] cookies;

    RESTfulRequest getForward(String resource) {
        RESTfulRequest r = new RESTfulRequest();
        r.req = req;
        r.resource = resource;
        r.parameter.putAll(parameter);
        if (fields != null) {
            r.fields = new HashMap<>(fields);
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

    void addField(String name, HttpRequestFile file) {
        if (fields == null) {
            fields = new HashMap<>();
        }
        this.fields.put(name, file);
    }

    public String getHeader(String name) {
        return req.getHeader(name);
    }

    public String[] getHeaderAsArray(String name) {
        String header = req.getHeader(name);
        if (header == null) return new String[0];
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
        if (fields == null) {
            return null;
        }
        HttpRequestFile[] array = new HttpRequestFile[fields.size()];
        fields.values().toArray(array);
        return array;
    }

    public HttpRequestFile getFile(String name) {
        return fields == null ? null : fields.get(name);
    }

    public String getContentType() {
        return req.getContentType();
    }

    public ServletInputStream getBody() throws IOException {
        return req.getInputStream();
    }

    public byte[] getBodyAsByte() {
        try {
            return StreamUtils.readByte(req.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getCharset() {
        return req.getCharacterEncoding();
    }

    public String getBodyAsText() {
        String cs = getCharset();
        if (cs == null) {
            return getBodyAsText(StandardCharsets.UTF_8);
        }
        return getBodyAsText(cs);
    }

    public String getBodyAsText(String charset) {
        return getBodyAsText(Charset.forName(charset));
    }

    public String getBodyAsText(Charset charset) {
        try {
            return StreamUtils.readString(req.getInputStream(), charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public int[] getIntParams(String name) {
        String[] params = getParams(name);
        if (params == null) {
            return null;
        }
        int l = params.length;
        int[] array = new int[l];
        try {
            for (int i = 0; i < array.length; i++) {
                array[i] = Integer.parseInt(params[i]);
            }
            return array;
        } catch (Exception e) {
            return null;
        }
    }

    public long[] getLongParams(String name) {
        String[] params = getParams(name);
        if (params == null) {
            return null;
        }
        int l = params.length;
        long[] array = new long[l];
        try {
            for (int i = 0; i < array.length; i++) {
                array[i] = Long.parseLong(params[i]);
            }
            return array;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean[] getBoolParams(String name) {
        String[] params = getParams(name);
        if (params == null) {
            return null;
        }
        int l = params.length;
        boolean[] array = new boolean[l];
        try {
            for (int i = 0; i < array.length; i++) {
                array[i] = Boolean.parseBoolean(getParameter(name));
            }
            return array;
        } catch (Exception e) {
            return null;
        }
    }

    public BigDecimal[] getDecimalParams(String name) {
        String[] params = getParams(name);
        if (params == null) {
            return null;
        }
        int l = params.length;
        BigDecimal[] array = new BigDecimal[l];
        try {
            for (int i = 0; i < array.length; i++) {
                array[i] = new BigDecimal(params[i]);
            }
            return array;
        } catch (Exception e) {
            return null;
        }
    }

    public String getParameter(String name, String fallback) {
        String param = getParameter(name);
        return param == null ? fallback : param;
    }

    public String getParameter(String name) {
        String value = this.parameter.get(name);
        if (value != null) {
            return value;
        }
        return req.getParameter(name);
    }

    public BigDecimal getDecimalParam(String name) {
        try {
            return new BigDecimal(getParameter(name));
        } catch (Exception e) {
            return null;
        }
    }

    public BigDecimal getDecimalParam(String name, BigDecimal fallback) {
        try {
            return new BigDecimal(getParameter(name));
        } catch (Exception e) {
            return fallback;
        }
    }

    public boolean getBoolParam(String name, boolean fallback) {
        if (has(name)) {
            return Boolean.parseBoolean(getParameter(name));
        }
        return fallback;
    }

    public boolean getBoolParam(String name) {
        return Boolean.parseBoolean(getParameter(name));
    }

    public int getIntParam(String name) {
        return getIntParam(name, 0);
    }

    public int getIntParam(String name, int fallback) {
        try {
            return Integer.parseInt(getParameter(name));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public long getLongParam(String name) {
        return getLongParam(name, 0);
    }

    public long getLongParam(String name, long fallback) {
        try {
            return Long.parseLong(getParameter(name));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * @return parameter value as int of current Resource
     */
    public int getIntValue() {
        return getIntParam(resource);
    }

    /**
     * @return parameter value of current Resource
     */
    public String getValue() {
        return getParameter(resource);
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
                cookies = new Cookie[0];
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

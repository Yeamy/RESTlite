package yeamy.restlite;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class RESTfulServlet extends GenericServlet {
    public static final String REQUEST = "RESTlite:Request";

    private static final long serialVersionUID = 1L;

    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PATCH = "PATCH";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        RESTfulRequest request = (RESTfulRequest) req.getAttribute(REQUEST);
        service(httpReq, httpResp, request);
    }

    public void service(HttpServletRequest req, HttpServletResponse resp, RESTfulRequest request)
            throws ServletException, IOException {
        switch (request.getMethod()) {
            // --------- REST API ----------------
            case METHOD_GET:
                doGet(request, resp);
                break;
            case METHOD_POST:
                doPost(request, resp);
                break;
            case METHOD_PATCH:
                doPatch(request, resp);
                break;
            case METHOD_PUT:
                doPut(request, resp);
                break;
            case METHOD_DELETE:
                doDelete(request, resp);
                break;
            // --------- Others ----------------
            case METHOD_OPTIONS:
                doOptions(resp);
                break;
            case METHOD_TRACE:
                doTrace(req, resp);
                break;
            case METHOD_HEAD:
                doGet(request, resp);
                break;
            default:
                do_(request, resp);
                break;
        }
    }

    private void notAllow(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void doPost(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllow(resp);
    }

    public void doPatch(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllow(resp);
    }

    public void doGet(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllow(resp);
    }

    public void doDelete(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllow(resp);
    }

    public void doPut(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllow(resp);
    }

    public void do_(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        notAllow(resp);
    }

    /**
     * @see jakarta.servlet.http.HttpServlet#doTrace(HttpServletRequest, HttpServletResponse)
     */
    void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String CRLF = "\r\n";
        StringBuilder buffer = new StringBuilder("TRACE ").append(req.getRequestURI()).append(" ")
                .append(req.getProtocol());

        Enumeration<String> reqHeaderEnum = req.getHeaderNames();

        while (reqHeaderEnum.hasMoreElements()) {
            String headerName = reqHeaderEnum.nextElement();
            buffer.append(CRLF).append(headerName).append(": ").append(req.getHeader(headerName));
        }

        buffer.append(CRLF);
        String data = buffer.toString();
        resp.setContentType("message/http");
        resp.setContentLength(data.length());
        ServletOutputStream out = resp.getOutputStream();
        out.print(data);
        out.close();
    }

    void doOptions(HttpServletResponse resp) throws ServletException, IOException {
        Method[] methods = getAllDeclaredMethods(this.getClass());
        Object[][] kvs = { //
                {"doHead", null}, //
                {"doGet", null}, //
                {"doPost", null}, //
                {"doPatch", null}, //
                {"doPut", null}, //
                {"doDelete", null}, //
                {"doOptions", ""}};
        int num = kvs.length;
        for (Method method : methods) {
            if (num == 0) {
                break;
            }
            for (Object[] kv : kvs) {
                if (kv[1] == null && kv[0].equals(method.getName())) {
                    kv[1] = "";
                    --num;
                }
            }
        }
        if (kvs[1][1] != null) {// get->head
            kvs[0][1] = "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object[] kv : kvs) {
            if (kv[1] != null) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(kv[0]);
            }
        }
        resp.setHeader("Allow", sb.toString());
    }

    private static Method[] getAllDeclaredMethods(Class<?> c) {
        Method[] parentMethods = c.equals(RESTfulServlet.class)
                ? null : getAllDeclaredMethods(c.getSuperclass());
        Method[] thisMethods = c.getDeclaredMethods();

        if ((parentMethods != null) && (parentMethods.length > 0)) {
            Method[] allMethods = new Method[parentMethods.length + thisMethods.length];
            System.arraycopy(parentMethods, 0, allMethods, 0, parentMethods.length);
            System.arraycopy(thisMethods, 0, allMethods, parentMethods.length, thisMethods.length);

            thisMethods = allMethods;
        }

        return thisMethods;
    }

}
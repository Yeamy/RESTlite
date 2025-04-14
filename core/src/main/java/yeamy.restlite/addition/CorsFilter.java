package yeamy.restlite.addition;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.utils.TextUtils;

import java.io.IOException;

/**
 * Filter solve CORS<br>
 * learn more about <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CORS">Cross-Origin Resource Sharing (CORS)</a>
 */
public abstract class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Object a = request.getAttribute(RESTfulRequest.REQUEST);
        if (a instanceof RESTfulRequest req) {
            String origin = req.getHeader("Origin");
            if (TextUtils.isNotEmpty(origin)) {
                CorsHandle handle = new CorsHandle(req, (HttpServletResponse) response);
                switch (req.getMethod()) {
                    case "OPTIONS" -> preflightRequest(origin, handle);
                    case "GET", "POST", "HEAD" -> doCorsRequest(origin, handle);
                }
                if (handle.isIntercept()) {
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * when http method is OPTIONS execute this method
     *
     * @param origin The HTTP Origin request header indicates the origin (scheme, hostname, and port) that caused the request.
     * @param handle handle class for cors
     */
    protected abstract void preflightRequest(String origin, CorsHandle handle);


    /**
     * execute this method when http method in: GET, POST, HEAD
     * <pre>{@code
     * protected void doCorsRequest(String origin, CorsHandle handle) {
     *     // check the origin handle.getRequestOrigin();
     *     handle.setAllowOrigin("https://foo.example"); // use * to allow all
     *     // check method handle.getRequestMethods();
     *     handle.setAllowMethods("GET", "POST", "OPTIONS");
     *     handle.setAllowMaxAge(86400);
     *     handle.addVary("Accept-Encoding, Origin");
     *     ...
     * }
     * }</pre>
     *
     * @param origin The HTTP Origin request header indicates the origin (scheme, hostname, and port) that caused the request.
     * @param handle handle class for cors
     */
    protected abstract void doCorsRequest(String origin, CorsHandle handle);

}

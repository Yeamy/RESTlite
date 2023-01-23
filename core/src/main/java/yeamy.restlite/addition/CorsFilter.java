package yeamy.restlite.addition;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.utils.TextUtils;

import java.io.IOException;

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

    protected abstract void preflightRequest(String origin, CorsHandle handle);

    protected abstract void doCorsRequest(String origin, CorsHandle handle);

}

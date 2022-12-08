package yeamy.restlite;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

public class DispatchFilter implements Filter {

    private final ArrayList<RESTliteFilter> filters = new ArrayList<>();

    @Override
    public void init(FilterConfig config) throws ServletException {
        RESTliteFilter[] fs = createFilters();
        if (fs != null) {
            for (RESTliteFilter f : fs) {
                filters.add(f);
                f.init(config);
            }
        }
    }

    protected RESTliteFilter[] createFilters() {
        return null;
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            try {
                RESTfulRequest request = RESTfulRequest.get(req);
                HttpServletResponse httResp = (HttpServletResponse) resp;
                for (RESTliteFilter f : filters) {
                    if (f.intercept(request, httResp)) {
                        return;
                    }
                }
                dispatch(request, httResp);
            } catch (ClassCastException e) {
                e.printStackTrace();
                chain.doFilter(req, resp);
            }
        }
        chain.doFilter(req, resp);
    }

    /**
     * dispatch request to resource, false intercept
     */
    protected void dispatch(RESTfulRequest request, HttpServletResponse resp) throws ServletException, IOException {
        HttpServletRequest req = request.getRequest();
        req.getRequestDispatcher('/' + request.getResource()).forward(req, resp);
    }

    @Override
    public void destroy() {
        for (RESTliteFilter f : filters) {
            f.destroy();
        }
    }
}

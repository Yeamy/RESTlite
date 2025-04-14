package yeamy.restlite;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * rest-lite internal web filter dispatch request to correct servlet
 */
public class DispatchFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            try {
                RESTfulRequest request = RESTfulRequest.get(req);
                HttpServletResponse httResp = (HttpServletResponse) resp;
                if (!request.dispatch) {
                    dispatch(request, httResp);
                    return;
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
                chain.doFilter(req, resp);
            }
        }
        chain.doFilter(req, resp);
    }

    /**
     * dispatch request to resource, false intercept
     * @param request http request
     * @param resp http response
     * @throws IOException if an I/O error occurs during this filter's processing of the request
     * @throws ServletException if the processing fails for any other reason
     */
    protected void dispatch(RESTfulRequest request, HttpServletResponse resp) throws ServletException, IOException {
        HttpServletRequest req = request.getRequest();
        req.getRequestDispatcher('/' + request.getResource()).forward(req, resp);
    }

}

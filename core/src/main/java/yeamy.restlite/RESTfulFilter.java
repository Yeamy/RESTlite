package yeamy.restlite;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static yeamy.restlite.RESTfulRequest.REQUEST;

public interface RESTfulFilter extends Filter {

    @Override
    default void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        if (request.getAttribute(REQUEST) instanceof RESTfulRequest req) {
            doFilter(req, resp, chain);
        } else {
            doOriginal((HttpServletRequest) request, resp, chain);
        }
    }

    void doFilter(RESTfulRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException;

    default void doOriginal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }
}

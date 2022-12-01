package yeamy.restlite.addition;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.RESTliteFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * add PATCH, let PATCH, PUT support Body on Tomcat via Reflection
 *
 * @author Yeamy
 */
public interface FilterBridge extends Filter, RESTliteFilter {

    @Override
    default void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    default boolean intercept(RESTfulRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpServletRequest request = req.getRequest();
        AtomicBoolean b = new AtomicBoolean(true);
        doFilter(request, resp, (q, r) -> b.set(false));
        return b.get();
    }

    @Override
    default void destroy() {
    }
}

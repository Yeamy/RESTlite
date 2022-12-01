package yeamy.restlite;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface RESTliteFilter {
    default void init(FilterConfig config) throws ServletException {
    }

    /**
     * @return <b>true</b>: intercept and stop here;<br> <b>false</b>: continue other filter
     */
    boolean intercept(RESTfulRequest req, HttpServletResponse resp) throws IOException, ServletException;

    default void destroy() {
    }

}

package example;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.RESTliteFilter;
import yeamy.restlite.annotation.Interceptor;

import java.io.IOException;

@Interceptor(index = 1)
public class MyInterceptor implements RESTliteFilter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public boolean intercept(RESTfulRequest req, HttpServletResponse resp) throws IOException, ServletException {
        return false;
    }

    @Override
    public void destroy() {
    }
}

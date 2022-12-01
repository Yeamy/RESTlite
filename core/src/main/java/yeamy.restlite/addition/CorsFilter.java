package yeamy.restlite.addition;

import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.RESTliteFilter;
import yeamy.utils.TextUtils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class CorsFilter implements RESTliteFilter {
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public boolean intercept(RESTfulRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String origin = req.getHeader("Origin");
        if (TextUtils.isEmpty(origin)) {
            return false;
        }
        CorsHandle handle = new CorsHandle(req, resp);
        switch (req.getMethod()) {
            case "OPTIONS":
                preflightRequest(origin, handle);
                break;
            case "GET":
            case "POST":
            case "HEAD":
                doCorsRequest(origin, handle);
        }
        return handle.isIntercept();
    }

    protected abstract void preflightRequest(String origin, CorsHandle handle);

    protected abstract void doCorsRequest(String origin, CorsHandle handle);

    @Override
    public void destroy() {
    }
}

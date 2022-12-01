package yeamy.restlite.permission;

import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.addition.TextPlainResponse;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SimplePermissionFilter extends PermissionFilter {

    @Override
    protected PermissionManager createPermissionManager(FilterConfig config) {
        return new LocalPermissionManager();
    }

    @Override
    protected String getAccount(RESTfulRequest request) {
        return request.getHeader("token");
    }

    @Override
    public void doDeny(HttpServletResponse resp) throws IOException, ServletException {
        new TextPlainResponse(HttpServletResponse.SC_FORBIDDEN, "no permission").write(resp);
    }

    @Override
    public void doNoAccount(HttpServletResponse resp) throws IOException, ServletException {
        new TextPlainResponse(HttpServletResponse.SC_UNAUTHORIZED, "no account").write(resp);
    }

}

package yeamy.restlite.permission;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.addition.TextPlainResponse;

import java.io.IOException;

/**
 * Simple PermissionFilter with LocalPermissionManager.
 * Access account with http header "X-Api-Token"
 */
public class SimplePermissionFilter extends PermissionFilter {
    public static final String HEADER_API_TOKEN = "X-Api-Token";

    @Override
    protected PermissionManager createPermissionManager(FilterConfig config) {
        return new LocalPermissionManager();
    }

    @Override
    protected String getAccount(RESTfulRequest request) {
        return request.getHeader(HEADER_API_TOKEN);
    }

    @Override
    public void doDeny(RESTfulRequest request, HttpServletResponse resp) throws IOException {
        new TextPlainResponse(HttpServletResponse.SC_FORBIDDEN, "no permission").write(resp);
    }

    @Override
    public void doNoAccount(RESTfulRequest request, HttpServletResponse resp) throws IOException {
        new TextPlainResponse(HttpServletResponse.SC_UNAUTHORIZED, "no account").write(resp);
    }

}

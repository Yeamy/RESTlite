package yeamy.restlite.permission;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import yeamy.restlite.RESTfulRequest;

import java.io.IOException;
import java.io.Writer;

/**
 * @see SimplePermissionFilter
 */
public abstract class PermissionFilter implements Filter {
    private PermissionManager manager;

    @Override
    public void init(FilterConfig config) throws ServletException {
        PermissionManager m = PermissionManager.getInstance();
        if (m == null) {
            m = createPermissionManager(config);
            PermissionManager.setInstance(m);
        }
        this.manager = m;
    }

    /**
     * create PermissionManager
     *
     * @param config The configuration information associated with the filter instance being initialised
     * @see LocalPermissionManager
     * @see RedisPermissionManager
     */
    protected abstract PermissionManager createPermissionManager(FilterConfig config);

    public final PermissionManager getPermissionManager() {
        return manager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Object a = request.getAttribute(RESTfulRequest.REQUEST);
        if (a instanceof RESTfulRequest) {
            RESTfulRequest req = (RESTfulRequest) a;
            Account account = manager.getAccount(getAccount(req));
            if (manager.isAllow(account, req.getResource(), req.getMethod(), req.getParams().keySet())) {
                chain.doFilter(request, response);
            } else if (account == null) {
                doNoAccount(req, (HttpServletResponse) response);
            } else {
                doDeny(req, (HttpServletResponse) response);
            }
        } else {
            doNoRESTfulRequest(request, response, chain);
        }
    }

    /**
     * do nothing if no RESTfulRequest
     */
    protected void doNoRESTfulRequest(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    protected abstract String getAccount(RESTfulRequest request);

    protected abstract void doDeny(RESTfulRequest req, HttpServletResponse resp) throws IOException, ServletException;

    protected abstract void doNoAccount(RESTfulRequest req, HttpServletResponse resp) throws IOException, ServletException;

    @Override
    public void destroy() {
        manager.destroy();
    }
}

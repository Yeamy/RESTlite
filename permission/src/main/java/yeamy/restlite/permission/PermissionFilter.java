package yeamy.restlite.permission;

import yeamy.restlite.RESTfulRequest;
import yeamy.restlite.RESTliteFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class PermissionFilter implements RESTliteFilter {
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

    protected abstract PermissionManager createPermissionManager(FilterConfig config);

    public final PermissionManager getPermissionManager() {
        return manager;
    }

    @Override
    public boolean intercept(RESTfulRequest req, HttpServletResponse resp) throws IOException,
            ServletException {
        Account account = manager.getAccount(getAccount(req));
        if (manager.isAllow(account, req.getResource(), req.getMethod(), req.getParams().keySet())) {
            return false;
        } else if (account == null) {
            doNoAccount(resp);
        } else {
            doDeny(resp);
        }
        return true;
    }

    protected abstract String getAccount(RESTfulRequest request);

    public abstract void doDeny(HttpServletResponse resp) throws IOException, ServletException;

    public abstract void doNoAccount(HttpServletResponse resp) throws IOException, ServletException;

    @Override
    public void destroy() {
        manager.destroy();
    }
}

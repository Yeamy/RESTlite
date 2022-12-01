package yeamy.restlite.permission;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

public abstract class PermissionWebFilter implements Filter {
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
    public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        Request req = toRequest(request);
        Account account = manager.getAccount(req.getAccount());
        if (manager.isAllow(account, req.getResource(), req.getMethod(), req.getParamName())) {
            chain.doFilter(request, resp);
        } else if (account == null) {
            doNoAccount(resp);
        } else {
            doDeny(resp);
        }
    }

    protected abstract Request toRequest(ServletRequest request);

    public void doDeny(ServletResponse resp) throws IOException, ServletException {
        HttpServletResponse r = (HttpServletResponse) resp;
        r.setStatus(HttpServletResponse.SC_FORBIDDEN);
        r.setContentType("text/plain");
        try (Writer w = r.getWriter()) {
            w.write("No permission");
        }
    }

    public void doNoAccount(ServletResponse resp) throws IOException, ServletException {
        HttpServletResponse r = (HttpServletResponse) resp;
        r.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        r.setContentType("text/plain");
        try (Writer w = r.getWriter()) {
            w.write("No account login");
        }
    }

    @Override
    public void destroy() {
        manager.destroy();
    }

    public interface Request {

        String getAccount();

        String getResource();

        String getMethod();

        Collection<String> getParamName();
    }
}

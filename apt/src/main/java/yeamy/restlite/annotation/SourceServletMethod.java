package yeamy.restlite.annotation;

import java.util.ArrayList;

import static yeamy.restlite.annotation.SupportType.T_HttpRequest;
import static yeamy.restlite.annotation.SupportType.T_TextPlainResponse;

class SourceServletMethod {
    protected final ProcessEnvironment env;
    protected final SourceServlet servlet;
    private final ArrayList<SourceImplMethodDispatcher> components = new ArrayList<>();
    protected final String httpMethod, nameOfServlet;

    public SourceServletMethod(ProcessEnvironment env, SourceServlet servlet, String httpMethod) {
        this.env = env;
        this.servlet = servlet;
        this.httpMethod = httpMethod;
        char[] src = httpMethod.toCharArray();
        char[] out = new char[src.length + 2];
        out[0] = 'd';
        out[1] = 'o';
        out[2] = src[0];
        for (int i = 1; i < src.length; i++) {
            out[i + 2] = (char) (src[i] + 32);
        }
        this.nameOfServlet = new String(out);
    }

    public final void addComponent(SourceImplMethodDispatcher component) {
        components.add(component);
    }

    protected void create(boolean containException, ArrayList<SourceImplMethodDispatcher> components) {
        servlet.imports(T_HttpRequest);
        servlet.imports("jakarta.servlet.http.HttpServletResponse");
        servlet.imports("jakarta.servlet.ServletException");
        servlet.imports("java.io.IOException");
        servlet.append("@Override public void ").append(nameOfServlet)
                .append("(RESTfulRequest _req, HttpServletResponse _resp) throws ServletException, IOException {");
        if (containException) servlet.append("try{");
        servlet.append("switch(_req.getServiceName()){");
        for (SourceImplMethodDispatcher component : components) {
            component.create(httpMethod);
        }
        if (components.size() >= 1 && allMethodHasArg()) {
            servlet.imports("yeamy.restlite.addition.NoMatchMethodException");
            servlet.append("default:{ onError(_req, _resp, new NoMatchMethodException(_req));}");
        }
        servlet.append(containException ? "}}catch(Exception ex){onError(_req,_resp,ex);}}" : "}}");
    }

    /**
     * cannot create server
     */
    protected void createError() {
        servlet.imports(T_HttpRequest);
        servlet.imports("jakarta.servlet.http.HttpServletResponse");
        servlet.imports("jakarta.servlet.ServletException");
        servlet.imports("java.io.IOException");
        servlet.append("@Override public void ").append(nameOfServlet)
                .append("(RESTfulRequest _req, HttpServletResponse _resp) throws ServletException, IOException {new ")
                .append(servlet.imports(T_TextPlainResponse))
                .append("(500, \"Server error!\");}");
    }

    public void create(boolean containException) {
        components.sort((m1, m2) -> {
            String k1 = m1.orderKey();
            String k2 = m2.orderKey();
            if (k1.length() < k2.length()) {
                return 1;
            } else if (k1.length() > k2.length()) {
                return -1;
            } else {
                for (int i = 0, l = k2.length(); i < l; i++) {
                    char c1 = k1.charAt(i);
                    char c2 = k2.charAt(i);
                    if (c1 < c2) {
                        return 1;
                    } else if (c1 > c2) {
                        return -1;
                    }
                }
                return 0;
            }
        });
        boolean ok = true;
        String cache = null;
        ArrayList<SourceImplMethodDispatcher> conflicts = new ArrayList<>();
        for (int i = 0, max = components.size() - 1; i <= max; i++) {
            SourceImplMethodDispatcher component = components.get(i);
            boolean eq = component.orderKey().equals(cache);
            if (eq) {
                conflicts.add(component);
            }
            if ((!eq || i == max) && conflicts.size() >= 2) {
                ok = false;
                StringBuilder msg = new StringBuilder("Conflict cause methods in class ").append(servlet.getImplClass())
                        .append(" with same request parameter(s):");
                for (SourceImplMethodDispatcher conflict : conflicts) {
                    msg.append(conflict.name()).append(", ");
                }
                env.error(msg.substring(0, msg.length() - 2));
            }
            if (!eq) {
                cache = component.orderKey();
                conflicts.clear();
                conflicts.add(component);
            }
        }
        if (ok) {
            create(containException, components);
        } else {
            createError();
        }
    }

    public boolean allMethodHasArg() {
        for (SourceImplMethodDispatcher component : components) {
            if (component.orderKey().length() == 0) {
                return false;
            }
        }
        return true;
    }
}

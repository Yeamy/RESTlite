package yeamy.restlite.annotation;

import java.util.ArrayList;
import java.util.HashSet;

import static yeamy.restlite.annotation.SupportType.T_HttpRequest;
import static yeamy.restlite.annotation.SupportType.T_TextPlainResponse;

class SourceServletMethod {
    protected final ProcessEnvironment env;
    protected final SourceServlet servlet;
    private final ArrayList<SourceImplMethodDispatcher> dispatchers = new ArrayList<>();
    protected final String httpMethod, nameInServlet;

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
        this.nameInServlet = new String(out);
    }

    public final void addComponent(SourceImplMethodDispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    protected void create(boolean hasOnError, ArrayList<SourceImplMethodDispatcher> dispatchers) {
        servlet.imports(T_HttpRequest);
        servlet.imports("jakarta.servlet.http.HttpServletResponse");
        servlet.imports("jakarta.servlet.ServletException");
        servlet.imports("java.io.IOException");
        servlet.append("@Override public void ").append(nameInServlet)
                .append("(RESTfulRequest _req, HttpServletResponse _resp) throws ServletException, IOException {");
        HashSet<String> throwTypes = new HashSet<>();
        dispatchers.forEach(e -> throwTypes.addAll(e.throwTypes()));
        if (hasOnError || throwTypes.size() > 0) servlet.append("try{");
        servlet.append("switch(_req.getServiceName()){");
        for (SourceImplMethodDispatcher dispatcher : dispatchers) {
            dispatcher.create(httpMethod);
        }
        if (dispatchers.size() >= 1 && allMethodHasArg()) {
            servlet.imports("yeamy.restlite.addition.NoMatchMethodException");
            servlet.append("default:{ onError(_req, _resp, new NoMatchMethodException(_req));}");
        }
        if (hasOnError) {
            servlet.append("}}catch(Exception _ex){onError(_req,_resp,_ex);}");
        } else if (throwTypes.size() > 0) {
            if (throwTypes.contains("yeamy.restlite.annotation.ProcessException")) {
                servlet.append("}catch(").append(servlet.imports("yeamy.restlite.annotation.ProcessException"))
                        .append(" _ex){_ex.getResponse().write(_resp);");
            }
            servlet.append("}}catch(Exception _ex){new ExceptionResponse(_ex).write(_resp);}");
        }
        servlet.append('}');
    }

    /**
     * cannot create server
     */
    protected void createFallback() {
        servlet.imports(T_HttpRequest);
        servlet.imports("jakarta.servlet.http.HttpServletResponse");
        servlet.imports("jakarta.servlet.ServletException");
        servlet.imports("java.io.IOException");
        servlet.append("@Override public void ").append(nameInServlet)
                .append("(RESTfulRequest _req, HttpServletResponse _resp) throws ServletException, IOException {new ")
                .append(servlet.imports(T_TextPlainResponse))
                .append("(500, \"Server error!\");}");
    }

    public void create(boolean hasOnError) {
        dispatchers.sort((m1, m2) -> {
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
        for (int i = 0, max = dispatchers.size() - 1; i <= max; i++) {
            SourceImplMethodDispatcher dispatcher = dispatchers.get(i);
            boolean eq = dispatcher.orderKey().equals(cache);
            if (eq) {
                conflicts.add(dispatcher);
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
                cache = dispatcher.orderKey();
                conflicts.clear();
                conflicts.add(dispatcher);
            }
        }
        if (ok) {
            create(hasOnError, dispatchers);
        } else {
            createFallback();
        }
    }

    public boolean allMethodHasArg() {
        for (SourceImplMethodDispatcher dispatcher : dispatchers) {
            if (dispatcher.orderKey().length() == 0) {
                return false;
            }
        }
        return true;
    }
}

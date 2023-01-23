package yeamy.restlite;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;

public abstract class RESTfulServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getMethod()) {
            case "GET", "POST", "DELETE", "PUT", "HEAD", "OPTIONS" -> super.service(req, resp);
            case "PATCH" -> doPatch(RESTfulRequest.get(req), resp);
            default -> do_(RESTfulRequest.get(req), resp);
        }
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(RESTfulRequest.get(req), resp);
    }

    public void doPost(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req.getRequest(), resp);
    }

    public void doPatch(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(RESTfulRequest.get(req), resp);
    }

    public void doGet(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req.getRequest(), resp);
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDelete(RESTfulRequest.get(req), resp);
    }

    public void doDelete(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req.getRequest(), resp);
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPut(RESTfulRequest.get(req), resp);
    }

    public void doPut(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req.getRequest(), resp);
    }

    public void do_(RESTfulRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private String cacheOptions;

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (cacheOptions == null) {
            Method[] methods = getAllDeclaredMethods(this.getClass());
            Object[][] kvs = { //
                    {"doHead", null}, //
                    {"doGet", null}, //
                    {"doPost", null}, //
                    {"doPatch", null}, //
                    {"doPut", null}, //
                    {"doDelete", null}, //
                    {"doOptions", ""}};
            int num = kvs.length;
            for (Method method : methods) {
                if (num == 0) {
                    break;
                }
                for (Object[] kv : kvs) {
                    if (kv[1] == null && kv[0].equals(method.getName())) {
                        kv[1] = "";
                        --num;
                    }
                }
            }
            if (kvs[1][1] != null) {// get->head
                kvs[0][1] = "";
            }
            StringBuilder sb = new StringBuilder();
            for (Object[] kv : kvs) {
                if (kv[1] != null) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(kv[0]);
                }
            }
            cacheOptions = sb.toString();
        }
        resp.setHeader("Allow", cacheOptions);
    }

    private static Method[] getAllDeclaredMethods(Class<?> c) {
        Method[] parentMethods = c.equals(RESTfulServlet.class)
                ? null : getAllDeclaredMethods(c.getSuperclass());
        Method[] thisMethods = c.getDeclaredMethods();

        if ((parentMethods != null) && (parentMethods.length > 0)) {
            Method[] allMethods = new Method[parentMethods.length + thisMethods.length];
            System.arraycopy(parentMethods, 0, allMethods, 0, parentMethods.length);
            System.arraycopy(thisMethods, 0, allMethods, parentMethods.length, thisMethods.length);

            thisMethods = allMethods;
        }

        return thisMethods;
    }

}
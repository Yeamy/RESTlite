package yeamy.restlite.annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static yeamy.restlite.annotation.SupportType.*;

class SourceMethodOnError {
    private static final String T_Exception = "java.lang.Exception";
    private static final String T_HttpServlet = "jakarta.servlet.http.HttpServlet";

    protected final ProcessEnvironment env;
    protected final SourceServlet servlet;
    private final ExecutableElement method;

    SourceMethodOnError(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method) {
        this.env = env;
        this.servlet = servlet;
        this.method = method;
    }

    public final void create() {
        servlet.append("public void onError(RESTfulRequest _req, HttpServletResponse _resp, Exception e) throws ServletException, IOException");
        servlet.append("{this._impl.").append(method.getSimpleName()).append('(');
        int l = servlet.length();
        for (VariableElement e : method.getParameters()) {
            TypeMirror t = e.asType();
            String clz = t.toString();
            switch (clz) {
                case T_Exception -> servlet.append(e.getSimpleName());
                case T_RESTfulRequest -> servlet.append("_req");
                case T_HttpServletRequest -> servlet.append("_req.getRequest()");
                case T_HttpServletResponse -> servlet.append("_resp");
                case T_HttpServlet -> servlet.append("this");
                default -> servlet.append(ProcessEnvironment.inValidTypeValue(t));
            }
            servlet.append(',');
        }
        if (servlet.length() > l) {
            servlet.deleteLast(1);
        }
        servlet.append(");}");
    }

}

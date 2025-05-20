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
            a:
            switch (t.getKind()) {
                case BOOLEAN:
                    servlet.append("false");
                    break;
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                    servlet.append("0");
                    break;
                case CHAR:
                    servlet.append("(char)0");
                    break;
                case BYTE:
                    servlet.append("(byte)0");
                    break;
                case DECLARED:
                    switch (clz) {
                        case T_String:
                            servlet.append("null");
                            break a;
                        case T_Exception:
                            servlet.append(e.getSimpleName());
                            break a;
                        case T_RESTfulRequest:
                            servlet.append("_req");
                            break a;
                        case T_HttpServletRequest:
                            servlet.append("_req.getRequest()");
                        case T_HttpServletResponse:
                            servlet.append("_resp");
                            break a;
                        case T_HttpServlet:
                            servlet.append("this");
                            break a;
                    }
                case ARRAY:
                default:
                    servlet.append("null/* not support type ").append(clz).append(" */");
                    break;
            }
            servlet.append(',');
        }
        if (servlet.length() > l) {
            servlet.deleteLast(1);
        }
        servlet.append(");}");
    }

}

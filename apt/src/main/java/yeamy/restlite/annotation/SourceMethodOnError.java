package yeamy.restlite.annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static yeamy.restlite.annotation.SourceHttpMethodComponent.HANDLER;
import static yeamy.restlite.annotation.SupportType.*;

class SourceMethodOnError {
    private static final String T_Exception = "java.lang.Exception";
    private static final String T_HttpResponse = "yeamy.restlite.HttpResponse";

    protected final ProcessEnvironment env;
    protected final SourceServlet servlet;
    private ExecutableElement method;
    private boolean intercept;

    SourceMethodOnError(ProcessEnvironment env, SourceServlet servlet) {
        this.env = env;
        this.servlet = servlet;
    }

    public void setMethod(ExecutableElement method, boolean intercept) {
        this.method = method;
        this.intercept = intercept;
    }

    public final void create() {
        servlet.append("public void onError(RESTfulRequest _req, HttpServletResponse _resp, Exception e) throws ServletException, IOException");
        if (method == null) {
            servlet.append("{new ")
                    .append(servlet.imports("yeamy.restlite.addition.ExceptionResponse"))
                    .append("(e).write(_resp);}");
        } else {
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
                                servlet.append("(String)_req.getRequest().getAttribute(\"")
                                        .append(HANDLER).append("\");");
                                break a;
                            case T_Exception:
                                servlet.append(e.getSimpleName());
                                break a;
                            case T_HttpRequest:
                                servlet.append("_req");
                                break a;
                            case T_HttpServletRequest:
                                servlet.append("_req.getRequest()");
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
            servlet.append(")");
            if (intercept) {
                TypeMirror rt = method.getReturnType();
                if (!(rt.getKind() == TypeKind.DECLARED
                        && env.isAssignable(rt, env.getTypeElement(T_HttpResponse).asType()))) {
                    servlet.imports("yeamy.restlite.addition.ExceptionResponse");
                    servlet.append(";new ExceptionResponse(e)");
                }
                servlet.append(".write(_resp);");
            } else {
                servlet.append(";throw e;");
            }
            servlet.append('}');
        }
    }

}

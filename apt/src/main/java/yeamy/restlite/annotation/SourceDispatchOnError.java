package yeamy.restlite.annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static yeamy.restlite.annotation.SupportType.T_HttpRequest;
import static yeamy.restlite.annotation.SupportType.T_HttpServletRequest;

/**
 * @see ERROR
 */
class SourceDispatchOnError extends SourceDispatch {
    private static final String T_Exception = "java.lang.Exception";

    public SourceDispatchOnError(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method) {
        super(env, servlet, method);
    }

    public void create() {
        servlet.append("return impl.").append(method.getSimpleName()).append('(');
        int l = servlet.length();
        for (VariableElement e : arguments) {
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
        servlet.append(");");
    }

}

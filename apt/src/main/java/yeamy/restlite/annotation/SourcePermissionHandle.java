package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

class SourcePermissionHandle {
    private static TypeMirror TM_HttpResponse;
    private final Name name;
    private final TypeMirror returnType;
    private final TypeElement importType;
    private final List<? extends VariableElement> params;

    public static SourcePermissionHandle get(ProcessEnvironment env, ExecutableElement method) {
        if (TM_HttpResponse == null) TM_HttpResponse = env.getTypeElement(T_HttpResponse).asType();
        Set<Modifier> modifiers = method.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
            env.warning("permission handle must be public static method");
            return null;
        }
        TypeMirror returnType = method.getReturnType();
        if (!env.isAssignable(returnType, TM_HttpResponse)) {
            env.warning("unsupported return type of permission handle");
            return null;
        }
        List<? extends VariableElement> params = method.getParameters();
        int i = 0;
        for (VariableElement p : params) {
            String tn = p.asType().toString();
            if (tn.equals(T_RESTfulRequest) || tn.equals(T_HttpServletRequest)) {
                i |= 1;
            } else if (tn.equals(T_String)) {
                i |= 2;
            }
        }
        for (TypeMirror tm : method.getThrownTypes()) {
            String tn = tm.toString();
            if (!tn.equals("jakarta.servlet.ServletException")
                    && !tn.equals("java.io.IOException")) {
                env.warning("unsupported throw type of permission handle");
                return null;
            }
        }
        if (i != 3) {
            env.warning("permission handle require param type: String and HttpServletRequest(or RESTfulRequest)");
            return null;
        }
        Name name = method.getSimpleName();
        TypeElement importType = (TypeElement) method.getEnclosingElement();
        return new SourcePermissionHandle(name, returnType, importType, params);
    }

    public SourcePermissionHandle(Name name, TypeMirror returnType, TypeElement importType, List<? extends VariableElement> params) {
        this.name = name;
        this.returnType = returnType;
        this.importType = importType;
        this.params = params;
    }

    void write(SourceServlet servlet, String permission) {
        if (permission.isEmpty()) return;
        if (returnType != null) {
            servlet.append(servlet.imports(returnType)).append(" __resp = ");
        }
        servlet.append(servlet.imports(importType)).append('.').append(name).append('(');
        for (VariableElement p : params) {
            TypeMirror tm = p.asType();
            String tn = tm.toString();
            switch (tn) {
                case T_RESTfulRequest->servlet.append("_req, ");
                case T_String-> servlet.append('"').append(permission).append("\", ");
                default -> servlet.append(ProcessEnvironment.inValidTypeValue(tm)).append(", ");
            }
        }
        servlet.deleteLast(2).append(");");
        if (returnType != null) {
            servlet.append("if (__resp != null) {__resp.write(_resp);return;}");
        }
    }

}

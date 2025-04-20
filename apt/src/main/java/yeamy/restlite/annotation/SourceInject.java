package yeamy.restlite.annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.T_ServletConfig;
import static yeamy.restlite.annotation.SupportType.T_ServletContext;

abstract class SourceInject extends SourceVariable {
    protected final TypeMirror returnType;

    public SourceInject(ProcessEnvironment env, VariableElement param, TypeMirror returnType) {
        super(env, param);
        this.returnType = returnType;
    }

    public void writeField(StringBuilder b, SourceServlet servlet) {
        if (!param.getModifiers().contains(Modifier.PRIVATE)) {
            b.append("_impl.").append(param.getSimpleName()).append('=');
            writeCreator(b, servlet);
            b.append(';');
            return;
        }
        ExecutableElement setter = env.findSetter(param);
        if (setter != null) {
            b.append("_impl.").append(setter.getSimpleName()).append('(');
            writeCreator(b, servlet);
            b.append(");");
            return;
        }
        String simpleName = param.getSimpleName().toString();
        TypeMirror typeMirror = param.getEnclosingElement().asType();
        env.error("Cannot assign " + typeMirror + "." + simpleName + " cause it's private and no setter found");
        b.append("/* Cannot assign _impl.").append(simpleName).append(" cause it's private and no setter found*/");
    }

    CharSequence writeArg(SourceServlet servlet) {
        String typeName = servlet.imports(returnType);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(param.getSimpleName()).append(" = ");
        writeCreator(b, servlet);
        b.append(';');
        return b;
    }

    static void writeParam(StringBuilder b, ExecutableElement method) {
        List<? extends VariableElement> ps = method.getParameters();
        if (ps.size() == 0) {
            b.append("()");
            return;
        }
        b.append('(');
        ps.forEach(param -> {
            TypeMirror tm = param.asType();
            TypeKind kind = tm.getKind();
            if (kind.isPrimitive()) {
                b.append(kind.equals(TypeKind.BOOLEAN) ? "false, " : "0,");
                return;
            }
            switch (tm.toString()) {
                case T_ServletConfig -> b.append("getServletConfig(),");
                case T_ServletContext -> b.append("getServletContext(),");
                default -> b.append("null,");
            }
        });
        b.replace(b.length() - 1, b.length(), ")");
    }

    protected abstract void writeCreator(StringBuilder b, SourceServlet servlet);

}

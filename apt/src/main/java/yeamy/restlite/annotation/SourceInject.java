package yeamy.restlite.annotation;

import javax.lang.model.element.*;
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

    /**
     * find inject field setter in impl, the setter name must be like setXxx(xxx).
     *
     * @return the setter or null
     */
    protected ExecutableElement findSetter() {
        StringBuilder sb = new StringBuilder("set").append(param.getSimpleName());
        char c = sb.charAt(3);
        if (c >= 'a' && c <= 'z') sb.setCharAt(3, (char) (c - 'a' + 'A'));// up case
        String name = sb.toString();
        for (Element li : param.getEnclosedElements()) {
            if (li.getKind() == ElementKind.METHOD && li.getSimpleName().toString().equals(name)) {
                ExecutableElement eli = (ExecutableElement) li;
                List<? extends VariableElement> ps = eli.getParameters();
                if (ps.size() == 1 && env.isAssignable(param.asType(), ps.get(0).asType())) {
                    return eli;
                }
            }
        }
        return null;
    }

    public void writeField(StringBuilder b, SourceServlet servlet) {
        ExecutableElement setter = findSetter();
        if (setter != null) {
            b.append("_impl.").append(setter.getSimpleName()).append('(');
            writeCreator(b, servlet);
            b.append(");");
        } else if (!param.getModifiers().contains(Modifier.PRIVATE)) {
            b.append("_impl.").append(param.getSimpleName()).append('=');
            writeCreator(b, servlet);
            b.append(';');
        } else {
            String simpleName = param.getSimpleName().toString();
            TypeMirror typeMirror = param.getEnclosingElement().asType();
            env.error("Cannot assign " + typeMirror + "." + simpleName + " cause it's private and no setter found");
            b.append("/* Cannot assign _impl.").append(simpleName).append(" cause it's private and no setter found*/");
        }
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

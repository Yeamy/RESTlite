package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

abstract class SourceInject extends SourceVariable {

    public SourceInject(ProcessEnvironment env, VariableElement param) {
        super(env, param);
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

//    protected VariableElement findField(TypeElement type) {
//        for (Element e : type.getEnclosedElements()) {
//            if (e.getKind().equals(ElementKind.FIELD)) {
//                Set<Modifier> modifiers = e.getModifiers();
//                VariableElement ve = (VariableElement) e;
//                if (modifiers.contains(Modifier.STATIC)
//                        && ve.asType().equals(param.asType())
//                        && isAssignable(e, modifiers)) {
//                    return ve;
//                }
//            }
//        }
//        return null;
//    }
//
//    protected boolean isAssignable(Element e, Set<Modifier> modifiers) {
//        return modifiers.contains(Modifier.PUBLIC)
//                || (!modifiers.contains(Modifier.PRIVATE) && env.isAssignable(typeMirror, e.asType()));
//    }

    public void writeField(StringBuilder b, SourceServlet servlet) {
        ExecutableElement setter = findSetter();
        if (setter != null) {
            b.append("_impl.").append(setter.getSimpleName()).append('(');
            writeFieldValue(b, servlet);
            b.append(");");
        } else if (!param.getModifiers().contains(Modifier.PRIVATE)) {
            b.append("_impl.").append(param.getSimpleName()).append('=');
            writeFieldValue(b, servlet);
            b.append(';');
        } else {
            String simpleName = param.getSimpleName().toString();
            TypeMirror typeMirror = param.getEnclosingElement().asType();
            env.error("Cannot assign " + typeMirror + "." + simpleName + " cause it's private and no setter found");
            b.append("/* Cannot assign _impl.").append(simpleName).append(" cause it's private and no setter found*/");
        }
    }

    protected abstract void writeFieldValue(StringBuilder b, SourceServlet servlet);

    public abstract CharSequence writeArg(SourceServlet servlet);

}

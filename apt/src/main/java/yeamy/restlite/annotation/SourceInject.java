package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import java.util.List;
import java.util.Set;

class SourceInject {
    private final SourceServlet servlet;
    private final ProcessEnvironment env;
    private final VariableElement element;
    private final String simpleName;

    public SourceInject(SourceServlet servlet, VariableElement element) {
        this.servlet = servlet;
        this.env = servlet.env;
        this.element = element;
        this.simpleName = element.getSimpleName().toString();
    }

    /**
     * find inject field setter in impl, the setter name must be like setXxx(xxx).
     *
     * @return the setter or null
     */
    private ExecutableElement findSetter() {
        StringBuilder sb = new StringBuilder("set").append(simpleName);
        char c = sb.charAt(3);
        if (c >= 'a' && c <= 'z') sb.setCharAt(3, (char) (c - 'a' + 'A'));
        String name = sb.toString();
        for (Element li : element.getEnclosedElements()) {
            if (li.getKind() == ElementKind.METHOD && li.getSimpleName().toString().equals(name)) {
                ExecutableElement eli = (ExecutableElement) li;
                List<? extends VariableElement> ps = eli.getParameters();
                if (ps.size() == 1 && env.isAssignable(element.asType(), ps.get(0).asType())) {
                    return eli;
                }
            }
        }
        return null;
    }

    private VariableElement findField(TypeElement type) {
        for (Element e : type.getEnclosedElements()) {
            if (e.getKind().equals(ElementKind.FIELD)) {
                Set<Modifier> modifiers = e.getModifiers();
                if (modifiers.contains(Modifier.STATIC)
                        && modifiers.contains(Modifier.FINAL)
                        && !modifiers.contains(Modifier.PRIVATE)) {
                    VariableElement ve = (VariableElement) e;
                    if (ve.asType().equals(element.asType())) {
                        return ve;
                    }
                }
            }
        }
        return null;
    }

    private static ExecutableElement findStaticMethod(TypeElement type) {
        for (Element e : type.getEnclosedElements()) {
            if (e.getKind().equals(ElementKind.METHOD)) {
                Set<Modifier> modifiers = e.getModifiers();
                if (modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.PRIVATE)) {
                    ExecutableElement ee = (ExecutableElement) e;
                    if (ee.getParameters().size() == 0) {
                        return ee;
                    }
                }
            }
        }
        return null;
    }

    private static ExecutableElement findConstructor(TypeElement type) {
        for (Element e : type.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement c = (ExecutableElement) e;
                if (!c.getModifiers().contains(Modifier.PRIVATE)
                        && c.getParameters().size() == 0) {
                    return c;
                }
            }
        }
        return null;
    }

    public void create(StringBuilder b) {
        ExecutableElement setter = null;
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            setter = findSetter();
            if (setter == null) {
                b.append("/* Cannot assign _impl.").append(simpleName)
                        .append(" case it's private and no setter found*/");
                return;
            }
        }
        boolean success = false;
        Inject inject = element.getAnnotation(Inject.class);
        if (inject == null) return;
        String creator = inject.creator();
        if (creator.length() > 0) {
            createByCreator(b, setter, creator, inject.tag(), false);
            return;
        }
        String typeName = element.asType().toString();
        TypeElement type = env.getTypeElement(typeName);
        Inject inject2 = type.getAnnotation(Inject.class);
        if (inject2 != null && inject2.creator().length() > 0) {
            createByCreator(b, setter, inject2.creator(), inject2.tag(), true);
            return;
        }
        SourceInjectProvider p = env.getInject(typeName);
        if (p != null) {
            success(b, setter, typeName, p.create(servlet));
        } else if (type.getKind() == ElementKind.INTERFACE) {
            fail(b, setter, "null/* Cannot create instance of interface */");
            env.error("Cannot find creator of interface: " + type);
        } else if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            fail(b, setter, "null/* Cannot create instance of abstract class */");
            env.error("Cannot find creator of abstract class: " + type);
        } else {
            Element e = findField(type);
            if (e != null) {
                success(b, setter, typeName, servlet.imports(type) + "." + e.getSimpleName());
                return;
            }
            e = findStaticMethod(type);
            if (e != null) {
                success(b, setter, typeName, servlet.imports(type) + '.' + e.getSimpleName() + "()");
            } else if (findConstructor(type) != null) {
                success(b, setter, typeName, "new " + servlet.imports(type) + "()");
            } else {
                fail(b, setter, "null/* Cannot find constructor */");
                env.error("Cannot find none params constructor of type: " + type);
            }
        }
    }

    private void createByCreator(StringBuilder b, ExecutableElement setter, String creator, String tag, boolean fromType) {
        TypeElement type = env.getTypeElement(creator);
        String typeName = element.asType().toString();
        String key = fromType ? typeName + ':' + tag : typeName;
        if (type == null) {
            fail(b, setter, "null/* Cannot find creator:" + creator + " */");
            env.error("Cannot find creator: " + creator + " of type " + typeName);
            return;
        } else if (tag.length() > 0) {
            for (Element e : type.getEnclosedElements()) {
                ElementKind kind = e.getKind();
                if (kind.equals(ElementKind.FIELD)) {
                    LinkTag t = e.getAnnotation(LinkTag.class);
                    if (t != null && TextUtils.in(tag, t.value())) {
                        success(b, setter, typeName, e.getSimpleName().toString());
                        return;
                    }
                } else if (kind.equals(ElementKind.CONSTRUCTOR)) {
                    LinkTag t = e.getAnnotation(LinkTag.class);
                    if (t != null && TextUtils.in(tag, t.value())) {
                        success(b, setter, key, "new " + servlet.imports(type) + "()");
                        return;
                    }
                } else if (kind.equals(ElementKind.METHOD)) {
                    LinkTag t = e.getAnnotation(LinkTag.class);
                    if (t != null && TextUtils.in(tag, t.value())) {
                        success(b, setter, key, servlet.imports(type) + '.' + e.getSimpleName() + "()");
                        return;
                    }
                }
            }
            fail(b, setter, "null/* Cannot find creator:" + creator + " with tag:" + tag + " */");
            env.error("Cannot find creator: " + creator + " with tag:" + tag + " of type " + typeName);
            return;
        }
        Element e = findField(type);
        if (e != null) {
            success(b, setter, key, servlet.imports(type) + "." + e.getSimpleName());
            return;
        }
        e = findStaticMethod(type);
        if (e != null) {
            success(b, setter, key, servlet.imports(type) + "." + e.getSimpleName() + "()");
            return;
        }
        if (env.isAssignable(type.asType(), element.asType())
                && findConstructor(type) != null) {
            success(b, setter, key, "new " + servlet.imports(type) + "()");
            return;
        }
        fail(b, setter, "null/* Cannot find creator:" + creator + " */");
        env.error("Cannot find creator: " + creator + " of type " + typeName);
        return;
    }

    /**
     * @param key <b>by field:</b> typeName + ':' + tag<br>
     *            <b>otherwise:</b> typeName
     */
    private void success(StringBuilder b, ExecutableElement setter, String key, String out) {
        b.append(servlet.imports("yeamy.utils.SingletonPool")).append(".getOrCreate(\"")
                .append(key).append("\",k->_impl.");
        if (setter != null) {
            b.append(setter.getSimpleName()).append('(').append(out).append(")");
        } else {
            b.append(simpleName).append('=').append(out);
        }
        b.append(");");
    }

    private void fail(StringBuilder b, ExecutableElement setter, String out) {
        b.append("_impl.");
        if (setter != null) {
            b.append(setter.getSimpleName()).append('(').append(out).append(")");
        } else {
            b.append(simpleName).append('=').append(out);
        }
        b.append(';');
    }

}

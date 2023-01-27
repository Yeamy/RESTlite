package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

class SourceInject {
    private final SourceServlet servlet;
    private final ProcessEnvironment env;
    private final TypeMirror typeMirror;
    private final VariableElement element;
    private final String simpleName;
    private boolean needClose = false;

    public SourceInject(SourceServlet servlet, VariableElement element) {
        this.servlet = servlet;
        this.env = servlet.env;
        this.element = element;
        this.typeMirror = element.getEnclosingElement().asType();
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
                VariableElement ve = (VariableElement) e;
                if (modifiers.contains(Modifier.STATIC)
                        && ve.asType().equals(element.asType())
                        && isAssignable(e, modifiers)) {
                    return ve;
                }
            }
        }
        return null;
    }

    private ExecutableElement findStaticMethod(TypeElement type) {
        for (Element e : type.getEnclosedElements()) {
            if (e.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement ee = (ExecutableElement) e;
                Set<Modifier> modifiers = e.getModifiers();
                if (modifiers.contains(Modifier.STATIC)
                        && ee.getParameters().size() == 0
                        && isAssignable(e, modifiers)) {
                    return ee;
                }
            }
        }
        return null;
    }

    private ExecutableElement findConstructor(TypeElement type) {
        for (Element e : type.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement c = (ExecutableElement) e;
                if (c.getParameters().size() == 0
                        && isAssignable(c, c.getModifiers())) {
                    return c;
                }
            }
        }
        return null;
    }

    public void createField(StringBuilder b) {
        ExecutableElement setter = null;
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            setter = findSetter();
            if (setter == null) {
                env.error("Cannot assign " + typeMirror + "." + simpleName
                        + " cause it's private and no setter found");
                b.append("/* Cannot assign _impl.").append(simpleName)
                        .append(" cause it's private and no setter found*/");
                return;
            }
        }
        String value = create(element.getAnnotation(Inject.class));
        b.append("_impl.");
        if (setter != null) {
            b.append(setter.getSimpleName()).append('(').append(value).append(')');
        } else {
            b.append(simpleName).append('=').append(value);
        }
        b.append(';');
    }

    public void createParameter(StringBuilder b) {
        String typeName = servlet.imports(element.asType().toString());
        String value = create(element.getAnnotation(Inject.class));// singleton never false
        b.append(typeName).append(' ').append(simpleName).append('=').append(value).append(';');
    }

    private String create(Inject inject) {
        boolean success = false;
        String creator = inject.creator();
        String key = inject.singleton().equals(Singleton.no)
                ? null
                : element.asType().toString();
        if (creator.length() > 0) {
            return createByCreator(creator, inject.tag(), key);
        }
        String typeName = element.asType().toString();
        TypeElement type = env.getTypeElement(typeName);
        Inject inject2 = type.getAnnotation(Inject.class);
        if (inject2 != null) {
            if (key == null && !inject2.singleton().equals(Singleton.no)) {
                key = element.asType().toString() + ':' + inject2.tag();
            }
            return createByCreator(inject2.creator(), inject2.tag(), key);
        }
        SourceInjectProvider p = env.getInject(typeName);
        if (p != null) {
            return success(key, p.create(servlet));
        } else if (type.getKind() == ElementKind.INTERFACE) {
            env.error("Cannot find creator of interface: " + type);
            return "null;/* Cannot create instance of interface */";
        } else if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator of abstract class: " + type);
            return "null;/* Cannot create instance of abstract class */";
        } else {
            Element e = findField(type);
            if (e != null) {
                return success(key, servlet.imports(type) + '.' + e.getSimpleName());
            }
            e = findStaticMethod(type);
            if (e != null) {
                return success(key, servlet.imports(type) + '.' + e.getSimpleName() + "()");
            } else if (findConstructor(type) != null) {
                return success(key, "new " + servlet.imports(type) + "()");
            } else {
                env.error("Cannot find none params constructor of type: " + type);
                return "null;/* Cannot find constructor */";
            }
        }
    }

    private boolean isAssignable(Element e, Set<Modifier> modifiers) {
        return modifiers.contains(Modifier.PUBLIC)
                || (env.isAssignable(typeMirror, e.asType())
                && !modifiers.contains(Modifier.PRIVATE));
    }

    private String createByCreator(String creator, String tag, String key) {
        TypeElement type = env.getTypeElement(creator);
        String typeName = element.asType().toString();
        if (type == null) {
            env.error("Cannot find creator: " + creator + " of type " + typeName);
            return "null;/* Cannot find creator:" + creator + " */";
        } else if (tag.length() > 0) {
            for (Element e : type.getEnclosedElements()) {
                ElementKind kind = e.getKind();
                Set<Modifier> modifiers = e.getModifiers();
                if (kind.equals(ElementKind.FIELD)
                        && modifiers.contains(Modifier.STATIC)
                        && modifiers.contains(Modifier.FINAL)
                        && isAssignable(e, modifiers)) {
                    LinkTag t = e.getAnnotation(LinkTag.class);
                    if (t != null && TextUtils.in(tag, t.value())) {
//                        if (singleton) {
//                            return success(singleton, typeName, e.getSimpleName().toString());
//                        } else {
//                        }
                        return e.getSimpleName().toString();
                    }
                } else if (kind.equals(ElementKind.CONSTRUCTOR)
                        && isAssignable(e, modifiers)) {
                    LinkTag t = e.getAnnotation(LinkTag.class);
                    if (t != null && TextUtils.in(tag, t.value())) {
                        return success(key, "new " + servlet.imports(type) + "()");
                    }
                } else if (kind.equals(ElementKind.METHOD)
                        && modifiers.contains(Modifier.STATIC)
                        && isAssignable(e, modifiers)) {
                    LinkTag t = e.getAnnotation(LinkTag.class);
                    if (t != null && TextUtils.in(tag, t.value())) {
                        return success(key, servlet.imports(type) + '.' + e.getSimpleName() + "()");
                    }
                }
            }
            env.error("Cannot find creator: " + creator + " with tag:" + tag + " of type " + typeName);
            return "null/* Cannot find creator:" + creator + " with tag:" + tag + " */";
        }
        Element e = findField(type);
        if (e != null) {
            return success(key, servlet.imports(type) + '.' + e.getSimpleName());
        }
        e = findStaticMethod(type);
        if (e != null) {
            return success(key, servlet.imports(type) + '.' + e.getSimpleName() + "()");
        }
        if (env.isAssignable(type.asType(), element.asType())
                && findConstructor(type) != null) {
            return success(key, "new " + servlet.imports(type) + "()");
        }
        env.error("Cannot find creator: " + creator + " of type " + typeName);
        return "null;/* Cannot find creator:" + creator + " */";
    }

    /**
     * @param key <b>by field:</b> typeName + ':' + tag<br>
     *            <b>otherwise:</b> typeName
     */
    private String success(String key, String value) {
        if (key != null) {
            return servlet.imports("yeamy.utils.SingletonPool") + ".getOrCreate(\"" + key + "\",k->" + value + ");";
        } else {
            needClose = env.isCloseable(element.asType());
        }
        return value + ";";
    }

    public boolean needClose() {
        return needClose;
    }

    public void createClose(StringBuilder b) {
        b.append(servlet.imports("yeamy.utils.StreamUtils")).append(".close(").append(simpleName).append(");");
    }
}

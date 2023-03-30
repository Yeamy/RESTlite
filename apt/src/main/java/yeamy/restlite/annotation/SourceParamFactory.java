package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class SourceParamFactory extends SourceParam {
    private final TypeMirror childType;
    private final ExecutableElement method;

    public static SourceParam inject(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child, String tag) {
        TypeElement factoryType = env.getTypeElement(factoryClass);
        if (factoryType == null) {
            env.error("No factory defend for type:" + child + " factory type:" + factoryClass);
            return SourceParamFail.INSTANCE;
        }
        List<? extends Element> elements = factoryType.getEnclosedElements();
        ExecutableElement method = findMethodByTag(elements, tag);
        if (method == null) {
            env.error("Cannot find method for type:" + child + " factory type:" + factoryClass);
            return SourceParamFail.INSTANCE;
        }
        return new SourceParamFactory(env, factoryType, child, method, samePackage, elements, ArgType.inject);
    }

    public static SourceParam inject(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child) {
        TypeElement factoryType = env.getTypeElement(factoryClass);
        if (factoryType != null) {
            List<? extends Element> elements = factoryType.getEnclosedElements();
            ExecutableElement method = findMethod(env, samePackage, child, elements);
            return new SourceParamFactory(env, factoryType, child, method, samePackage, elements, ArgType.inject);
        }
        env.error("No factory defend for type:" + child + " factory type:" + factoryClass);
        return SourceParamFail.INSTANCE;
    }

    public static SourceParam body(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child, String tag) {
        return partOrBody(env, samePackage, factoryClass, child, tag, ArgType.body);
    }

    public static SourceParam part(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child, String tag) {
        return partOrBody(env, samePackage, factoryClass, child, tag, ArgType.part);
    }

    private static SourceParam partOrBody(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child, String tag, ArgType argType) {
        TypeElement factoryType = env.getTypeElement(factoryClass);
        if (factoryType == null) {
            env.error("No factory defend for type:" + child + " factory type:" + factoryClass);
            return SourceParamFail.INSTANCE;
        }
        List<? extends Element> elements = factoryType.getEnclosedElements();
        ExecutableElement method = findMethodByTag(elements, tag);
        if (method == null) {
            env.error("Cannot find method for type:" + child + " factory type:" + factoryClass);
            return SourceParamFail.INSTANCE;
        }
        return new SourceParamFactory(env, factoryType, child, method, samePackage, elements, argType);
    }

    private static ExecutableElement findMethodByTag(List<? extends Element> elements, String tag) {
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                LinkTag[] tags = method.getAnnotationsByType(LinkTag.class);
                if (tags != null) {
                    for (LinkTag li : tags) {
                        if (TextUtils.in(tag, li.value())) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static SourceParam body(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child) {
        return partOrBody(env, samePackage, factoryClass, child, ArgType.body);
    }

    public static SourceParam part(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child) {
        return partOrBody(env, samePackage, factoryClass, child, ArgType.part);
    }

    private static SourceParam partOrBody(ProcessEnvironment env, boolean samePackage, String factoryClass, TypeMirror child, ArgType argType) {
        TypeElement factoryType = env.getTypeElement(factoryClass);
        if (factoryType == null) {
            env.error("No factory defend for type:" + child + " factory type:" + factoryClass);
            return SourceParamFail.INSTANCE;
        }
        List<? extends Element> elements = factoryType.getEnclosedElements();
        ExecutableElement method = findMethod(env, samePackage, child, elements);
        return new SourceParamFactory(env, factoryType, child, method, samePackage, elements, argType);
    }

    private static ExecutableElement findMethod(ProcessEnvironment env, boolean samePackage, TypeMirror child, List<? extends Element> elements) {
        LinkedList<ExecutableElement> methods = new LinkedList<>();
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                Set<Modifier> modifiers = element.getModifiers();
                if (modifiers.contains(Modifier.STATIC) && (samePackage || modifiers.contains(Modifier.PUBLIC))) {
                    ExecutableElement method = (ExecutableElement) element;
                    TypeMirror rt = method.getReturnType();
                    TypeKind rtk = rt.getKind();
                    if (rtk == TypeKind.TYPEVAR) {
                        Element e = env.asElement(rt);
                        if (e instanceof TypeParameterElement ee) {
                            String typeVar = "java.lang.Class<" + e + ">";
                            List<? extends TypeMirror> bounds = ee.getBounds();
                            for (TypeMirror bound : bounds) {
                                if (env.isAssignable(child, bound) && checkParam(method, typeVar)) {
                                    methods.add(method);
                                    break;
                                }
                            }
                        }
                    } else if (rtk != TypeKind.VOID && env.isAssignable(child, rt)
                            && checkParam(method)) {
                        methods.add(method);
                    }
                }
            }
        }
        for (ExecutableElement m : methods) {
            if (m.getModifiers().contains(Modifier.PUBLIC)) {
                return m;
            }
        }
        return methods.size() == 0 ? null : methods.getFirst();
    }

    private SourceParamFactory(ProcessEnvironment env,
                               TypeElement type,
                               TypeMirror childType,
                               ExecutableElement method,
                               boolean samePackage,
                               List<? extends Element> elements,
                               ArgType argType) {
        super(env, type, argType);
        this.childType = childType;
        this.method = method;
        init(method, samePackage, elements);
    }

    @Override
    protected String declaredArgument(SourceServlet servlet, VariableElement param) {
        TypeMirror rt = method.getReturnType();
        if (rt.getKind() == TypeKind.TYPEVAR) {
            Element e = env.asElement(rt);
            if (e instanceof TypeParameterElement) {
                String typeVar = "java.lang.Class<" + e + ">";
                if (param.asType().toString().equals(typeVar)) {
                    return servlet.imports(childType) + ".class";
                }
            }
        }
        return "null";
    }

    @Override
    public CharSequence toCharSequence(SourceServlet servlet, SourceArguments args, String name) {
        StringBuilder b = new StringBuilder(servlet.imports(childType)).append(" ").append(name);
        b.append(" = ").append(servlet.imports(type)).append('.').append(method.getSimpleName()).append("(");
        appendParam(method.getParameters(), servlet, args, b);
        b.append(");");
        return b;
    }

}

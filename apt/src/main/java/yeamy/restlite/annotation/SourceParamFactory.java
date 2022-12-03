package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class SourceParamFactory extends SourceParamCreator {
    private final TypeMirror childType;
    private final TypeElement factoryType;
    private final ExecutableElement method;

    public static SourceParamCreator body(ProcessEnvironment env, boolean samePackage, String factoryClass,
                                          TypeMirror child, String tag) {
        TypeElement factoryType = env.getTypeElement(factoryClass);
        if (factoryType == null) {
            return new SourceParamFail();
        }
        List<? extends Element> elements = factoryType.getEnclosedElements();
        ExecutableElement method = findMethodByTag(elements, tag);
        SourceParamFactory f = new SourceParamFactory(factoryType, child, method);
        f.init(env, factoryType.asType(), method, samePackage, elements);
        return f;
    }

    public static SourceParamCreator body(ProcessEnvironment env, boolean samePackage, String factoryClass,
                                          TypeMirror child) {
        TypeElement factoryType = env.getTypeElement(factoryClass);
        if (factoryType == null) {
            return new SourceParamFail();
        }
        List<? extends Element> elements = factoryType.getEnclosedElements();
        ExecutableElement method = findMethod(env, samePackage, child, elements);
        SourceParamFactory f = new SourceParamFactory(factoryType, child, method);
        f.init(env, factoryType.asType(), method, samePackage, elements);
        return f;
    }

    private SourceParamFactory(TypeElement factoryType, TypeMirror childType, ExecutableElement method) {
        this.childType = childType;
        this.factoryType = factoryType;
        this.method = method;
    }

    private static ExecutableElement findMethodByTag(List<? extends Element> elements, String tag) {
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                LinkTag[] tags = method.getAnnotationsByType(LinkTag.class);
                if (tags != null) {
                    for (LinkTag li : tags) {
                        if (li.value().equals(tag)) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ExecutableElement findMethod(ProcessEnvironment env, boolean samePackage, TypeMirror child,
                                                List<? extends Element> elements) {
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
                        if (e instanceof TypeParameterElement) {
                            String typeVar = "java.lang.Class<" + e + ">";//TODO
                            List<? extends TypeMirror> bounds = ((TypeParameterElement) e).getBounds();
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

    @Override
    public CharSequence toCharSequence(SourceParamChain chain, String name) {
        SourceServlet servlet = chain.getServlet();
        StringBuilder b = new StringBuilder(servlet.imports(childType.toString())).append(" ").append(name);
        b.append(" = ").append(servlet.imports(factoryType)).append('.').append(method.getSimpleName()).append("(");
        appendParam(method.getParameters(), chain, b);
        b.append(");");
        return b;
    }

    @Override
    public CharSequence toCharSequence(SourceParamChain chain) {
        StringBuilder b = new StringBuilder(chain.getServlet().imports(factoryType)).append('.')
                .append(method.getSimpleName()).append("(");
        appendParam(method.getParameters(), chain, b);
        b.append(")");
        return b;
    }

}

package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class SourceParamConstructor extends SourceParamCreator {
    private final TypeElement type;
    private final List<? extends VariableElement> parameters;

    public static SourceParamCreator body(ProcessEnvironment env, boolean samePackage, TypeElement type, String tag) {
        List<? extends Element> elements = type.getEnclosedElements();
        ExecutableElement constructor;
        List<? extends VariableElement> parameters;
        if (tag.length() > 0) {
            constructor = findConstructor(elements, tag);
            if (constructor == null) {
                return new SourceParamFail();
            }
            parameters = constructor.getParameters();
        } else {
            LinkedList<ExecutableElement> constructors = allConstructor(elements);
            if (constructors.size() == 0) {
                constructor = null;
                parameters = Collections.emptyList();
            } else {
                constructor = findConstructor(constructors, samePackage);
                parameters = constructor == null
                        ? Collections.emptyList()
                        : constructor.getParameters();
            }
        }
        SourceParamConstructor creator = new SourceParamConstructor(type, parameters);
        creator.init(env, type.asType(), constructor, samePackage, elements);
        return creator;
    }

    private SourceParamConstructor(TypeElement type, List<? extends VariableElement> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    private static ExecutableElement findConstructor(List<? extends Element> list, String tag) {
        for (Element element : list) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
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

    private static LinkedList<ExecutableElement> allConstructor(List<? extends Element> list) {
        LinkedList<ExecutableElement> methods = new LinkedList<>();
        for (Element enclosed : list) {// find constructor
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                methods.add((ExecutableElement) enclosed);
            }
        }
        return methods;
    }

    private static ExecutableElement findConstructor(LinkedList<ExecutableElement> methods, boolean samePackage) {
        Iterator<ExecutableElement> itr = methods.iterator();
        while (itr.hasNext()) {// find public
            ExecutableElement element = itr.next();
            if (!samePackage && !element.getModifiers().contains(Modifier.PUBLIC)) {
                itr.remove();
            } else if (!checkParam(element)) {
                itr.remove();
            }
        }
        for (ExecutableElement m : methods) {// public-first
            if (m.getModifiers().contains(Modifier.PUBLIC)) {
                return m;
            }
        }
        return methods.size() == 0 ? null : methods.getFirst();
    }

    @Override
    public CharSequence toCharSequence(SourceParamChain chain, String name) {
        String typeName = chain.getServlet().imports(type);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(name);
        b.append(" = new ").append(typeName).append("(");
        appendParam(parameters, chain, b);
        b.append(");");
        return b;
    }

    @Override
    public CharSequence toCharSequence(SourceParamChain chain) {
        String typeName = chain.getServlet().imports(type);
        StringBuilder b = new StringBuilder("new ").append(typeName).append("(");
        appendParam(parameters, chain, b);
        b.append(")");
        return b;
    }

}

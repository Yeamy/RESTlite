package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class SourceParamConstructor extends SourceParamCreator {
    private final List<? extends VariableElement> parameters;

    public static SourceParamCreator body(ProcessEnvironment env, boolean samePackage, TypeElement type, String tag) {
        List<? extends Element> elements = type.getEnclosedElements();
        ExecutableElement constructor;
        List<? extends VariableElement> parameters;
        if (tag.length() > 0) {
            constructor = findConstructor(elements, tag);
            if (constructor == null) {
                env.warning("not support body type " + type + " without annotation Creator");
                return SourceParamFail.INSTANCE;
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
        return new SourceParamConstructor(env, type, parameters, constructor, samePackage, elements);
    }

    private static ExecutableElement findConstructor(List<? extends Element> list, String tag) {
        for (Element element : list) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
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

    private SourceParamConstructor(ProcessEnvironment env,
                                   TypeElement type,
                                   List<? extends VariableElement> parameters,
                                   ExecutableElement constructor,
                                   boolean samePackage,
                                   List<? extends Element> elements) {
        super(env, type);
        this.parameters = parameters;
        init(constructor, samePackage, elements);
    }

    @Override
    protected String declaredArgument(SourceServlet servlet, VariableElement param) {
        return "null";
    }

    @Override
    public CharSequence toCharSequence(SourceServlet servlet, SourceArguments args, String name) {
        String typeName = servlet.imports(type);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(name);
        b.append(" = new ").append(typeName).append("(");
        appendParam(parameters, servlet, args, b);
        b.append(");");
        return b;
    }

}

package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

class SourceProcessorBody extends SourceProcessor {
    private final String charset;

    public static SourceProcessor get(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Body ann) {
        TypeMirror type = param.asType();
        String factoryClz = ProcessEnvironment.getAnnotationType(ann::processor);
        if (TextUtils.isNotEmpty(factoryClz)) {
            TypeElement factoryType = env.getTypeElement(factoryClz);
            if (factoryType != null) {
                boolean samePackage = isSamePackage(servlet, factoryType);
                String tag = ann.tag();
                List<? extends Element> elements = factoryType.getEnclosedElements();
                if (TextUtils.isEmpty(tag)) {
                    if (env.isAssignable(factoryType.asType(), type)) {
                        for (ExecutableElement constructor : allConstructor(elements, samePackage)) {
                            if (paramOk(constructor)) {
                                return new SourceProcessorBody(env, factoryType, type, constructor, samePackage, elements, ann.charset());
                            }
                        }
                    }
                    for (ExecutableElement method : findMethodByType(env, type, elements, samePackage)) {
                        if (paramOk(method)) {
                            return new SourceProcessorBody(env, factoryType, type, method, samePackage, elements, ann.charset());
                        }
                    }
                } else {
                    if (env.isAssignable(factoryType.asType(), type)) {
                        LinkedList<ExecutableElement> constructors = allConstructor(elements, samePackage);
                        ExecutableElement constructor = findConstructor(constructors, tag, samePackage);
                        if (constructor != null && paramOk(constructor)) {
                            return new SourceProcessorBody(env, factoryType, type, constructor, samePackage, elements, ann.charset());
                        }
                    }
                    ExecutableElement method = findMethodByTag(elements, tag, samePackage);
                    if (method != null && paramOk(method)) {
                        return new SourceProcessorBody(env, factoryType, type, method, samePackage, elements, ann.charset());
                    }
                }
            }
        }
        env.error("No factory defend for type:" + type + " cause factory type is empty");
        return SourceProcessorFail.INSTANCE;
    }

    private static boolean paramOk(ExecutableElement constructor) {
        for (VariableElement p : constructor.getParameters()) {
            if (p.getAnnotation(Inject.class) != null || p.getAnnotation(Part.class) != null) {
                return false;
            }
        }
        return true;
    }

    private SourceProcessorBody(ProcessEnvironment env,
                                TypeElement factoryType,
                                TypeMirror returnType,
                                ExecutableElement exec,
                                boolean samePackage,
                                List<? extends Element> elements,
                                String charset) {
        super(env, factoryType, exec, returnType);
        this.charset = charset;
        init(exec, samePackage, elements);
    }

    @Override
    protected boolean doPart(SourceServlet servlet, SourceArguments args, VariableElement p, StringBuilder b) {
        return false; // disable part
    }

    @Override
    public void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name) {
        args.addBody(name, isThrowable(), isCloseable(), isCloseThrow()).write(toCharSequence(servlet, args, name));
    }
}

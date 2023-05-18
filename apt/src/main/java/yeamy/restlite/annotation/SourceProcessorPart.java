package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceProcessorPart extends SourceProcessor {
    private final String alias;
    private final String charset;

    public static SourceProcessor get(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Part ann) {
        String name = ann.value();
        if (TextUtils.isEmpty(name)) name = param.getSimpleName().toString();
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
                                return new SourceProcessorPart(env, factoryType, type, constructor, samePackage, elements, name, ann.charset());
                            }
                        }
                    }
                    for (ExecutableElement method : findMethodByType(env, type, elements, samePackage)) {
                        if (paramOk(method)) {
                            return new SourceProcessorPart(env, factoryType, type, method, samePackage, elements, name, ann.charset());
                        }
                    }
                } else {
                    if (env.isAssignable(factoryType.asType(), type)) {
                        LinkedList<ExecutableElement> constructors = allConstructor(elements, samePackage);
                        ExecutableElement constructor = findConstructor(constructors, tag, samePackage);
                        if (constructor != null && paramOk(constructor)) {
                            return new SourceProcessorPart(env, factoryType, type, constructor, samePackage, elements, name, ann.charset());
                        }
                    }
                    ExecutableElement method = findMethodByTag(elements, tag, samePackage);
                    if (method != null && paramOk(method)) {
                        return new SourceProcessorPart(env, factoryType, type, method, samePackage, elements, name, ann.charset());
                    }
                }
            }
        }
        env.error("No factory defend for type:" + type + " factory type:" + factoryClz);
        return SourceProcessorFail.INSTANCE;

    }

    private static boolean paramOk(ExecutableElement constructor) {
        for (VariableElement p : constructor.getParameters()) {
            if (p.getAnnotation(Inject.class) != null || p.getAnnotation(Body.class) != null) {
                return false;
            }
        }
        return true;
    }

    private SourceProcessorPart(ProcessEnvironment env,
                                TypeElement factoryType,
                                TypeMirror returnType,
                                ExecutableElement exec,
                                boolean samePackage,
                                List<? extends Element> elements,
                                String alias,
                                String charset) {
        super(env, factoryType, exec, returnType);
        this.alias = alias;
        this.charset = charset;
        init(exec, samePackage, elements);
    }

    @Override
    protected boolean doBody(SourceArguments args, VariableElement p, StringBuilder b) {
        return false;// disable body
    }

    @Override
    protected boolean doPart(SourceServlet servlet, SourceArguments args, VariableElement p, StringBuilder b) {
        return super.doPart(servlet, args, p, b);
    }

    @Override
    public void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name) {
        args.addPart(name, alias, isThrowable(), isCloseable(), isCloseThrow()).write(toCharSequence(servlet, args, name));
    }

}

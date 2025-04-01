package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

class SourceArgsInject extends SourceArgs {
    private final SourceInject sourceInject;

    public static SourceArgs get(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Inject ann) {
        TypeMirror type = param.asType();
        SourceInjectProvider ip = env.getInjectProvider(type.toString());


        String factoryClz = ProcessEnvironment.getAnnotationType(ann::creator);
        if (TextUtils.isNotEmpty(factoryClz)) {
            TypeElement factoryType = env.getTypeElement(factoryClz);
            if (factoryType != null) {
                boolean samePackage = isSamePackage(servlet, factoryType);
                String tag = ann.tag();
                List<? extends Element> elements = factoryType.getEnclosedElements();
                if (TextUtils.isEmpty(tag)) {
                    if (env.isAssignable(factoryType.asType(), type)) {
                        LinkedList<ExecutableElement> constructors = allConstructor(elements, samePackage);
                        switch (constructors.size()) {
                            case 0:
                                break;
                            case 1:
                                return new SourceArgsInject(env, factoryType, type, constructors.get(0), samePackage, elements);
                            default:
                                env.error("More than one constructor in inject factory defend for type:" + type);
                                return SourceArgsFail.INSTANCE;
                        }
                    }
                    LinkedList<ExecutableElement> methods = findMethodByType(env, type, elements, samePackage);
                    switch (methods.size()) {
                        case 0:
                            break;
                        case 1:
                            return new SourceArgsInject(env, factoryType, type, methods.get(0), samePackage, elements);
                        default:
                            env.error("More than one method in inject factory defend for type:" + type + " " + param.getSimpleName());
                            return SourceArgsFail.INSTANCE;
                    }
                } else {
                    if (env.isAssignable(factoryType.asType(), type)) {
                        LinkedList<ExecutableElement> constructors = allConstructor(elements, samePackage);
                        ExecutableElement constructor = findConstructor(constructors, tag, samePackage);
                        if (constructor != null) {
                            return new SourceArgsInject(env, factoryType, type, constructor, samePackage, elements);
                        }
                    }
                    ExecutableElement method = findMethodByTag(elements, tag, samePackage);
                    if (method != null) {
                        return new SourceArgsInject(env, factoryType, type, method, samePackage, elements);
                    }
                }
            }
        }
        env.error("No factory defend for inject type:" + type + " cause factory type is empty");
        return SourceArgsFail.INSTANCE;
    }

    private SourceArgsInject(ProcessEnvironment env,
                             TypeElement factoryType,
                             TypeMirror returnType,
                             ExecutableElement exec,
                             boolean samePackage,
                             List<? extends Element> elements) {
        super(env, factoryType, exec, returnType);
        init(exec, samePackage, elements);
        sourceInject = null;
    }

//    private SourceArgsInject(SourceServlet servlet, VariableElement param) {
//        super(null, null, null, null);
//        sourceInject = new SourceInject(servlet, param);
//    }

    @Override
    public void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name) {
        if (sourceInject != null) {
            StringBuilder b = new StringBuilder();
            sourceInject.createParameter(b);
            args.addInject(name, false, false, false).write(b);
        } else {
            args.addInject(name, isThrowable(), isCloseable(), isCloseThrow()).write(toCharSequence(servlet, args, name));
        }
    }
}

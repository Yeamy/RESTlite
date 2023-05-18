package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

class SourceProcessorParam extends SourceProcessor {
    private final String alias;

    public static SourceProcessor get(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Param ann) {
        String factoryClz = ProcessEnvironment.getAnnotationType(ann::processor);
        if (TextUtils.isEmpty(factoryClz)) {
            return null;
        }
        String name = ann.value();
        if (TextUtils.isEmpty(name)) name = param.getSimpleName().toString();
        TypeMirror type = param.asType();
        TypeElement factoryType = env.getTypeElement(factoryClz);
        if (factoryType != null) {
            boolean samePackage = isSamePackage(servlet, factoryType);
            String tag = ann.tag();
            List<? extends Element> elements = factoryType.getEnclosedElements();
            if (TextUtils.isEmpty(tag)) {
                if (env.isAssignable(factoryType.asType(), type)) {
                    for (ExecutableElement constructor : allConstructor(elements, samePackage)) {
                        List<? extends VariableElement> ps = constructor.getParameters();
                        if (ps.size() == 1 && ps.get(0).asType().toString().equals(SupportType.T_String)) {
                            return new SourceProcessorParam(env, factoryType, type, constructor, samePackage, elements, name);
                        }
                    }
                }
                for (ExecutableElement method : findMethodByType(env, type, elements, samePackage)) {
                    List<? extends VariableElement> ps = method.getParameters();
                    if (ps.size() == 1 && ps.get(0).asType().toString().equals(SupportType.T_String)) {
                        return new SourceProcessorParam(env, factoryType, type, method, samePackage, elements, name);
                    }
                }
            } else {
                if (env.isAssignable(factoryType.asType(), type)) {
                    LinkedList<ExecutableElement> constructors = allConstructor(elements, samePackage);
                    ExecutableElement c = findConstructor(constructors, tag, samePackage);
                    if (c != null) {
                        List<? extends VariableElement> ps = c.getParameters();
                        if (ps.size() == 1 && ps.get(0).asType().toString().equals(SupportType.T_String)) {
                            return new SourceProcessorParam(env, factoryType, type, c, samePackage, elements, name);
                        }
                    }
                }
                ExecutableElement method = findMethodByTag(elements, tag, samePackage);
                if (method != null) {
                    List<? extends VariableElement> ps = method.getParameters();
                    if (ps.size() == 1 && ps.get(0).asType().toString().equals(SupportType.T_String)) {
                        return new SourceProcessorParam(env, factoryType, type, method, samePackage, elements, name);
                    }
                }
            }
        }
        env.error("No factory defend for type:" + type + " factory type:" + factoryClz);
        return SourceProcessorFail.INSTANCE;
    }

    private SourceProcessorParam(ProcessEnvironment env,
                                 TypeElement factoryType,
                                 TypeMirror returnType,
                                 ExecutableElement exec,
                                 boolean samePackage,
                                 List<? extends Element> elements,
                                 String alias) {
        super(env, factoryType, exec, returnType);
        this.alias = alias;
        init(exec, samePackage, elements);
    }

    @Override
    protected void appendArgument(SourceServlet servlet, SourceArguments args, VariableElement param, StringBuilder b) {
        b.append("_req.getParameter(\"").append(alias).append("\")");
    }

    @Override
    public void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name) {
        args.addParam(p.asType().toString(), name, alias).write(toCharSequence(servlet, args, name));
    }

}

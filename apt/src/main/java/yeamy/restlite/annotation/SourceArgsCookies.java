package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.T_Cookie;
import static yeamy.restlite.annotation.SupportType.T_Cookies;

class SourceArgsCookies extends SourceArgs {
    private static final String[] COOKIES_TYPES = {T_Cookies, T_Cookie};
    private final String type, alias;

    public static SourceArgs get(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Header ann) {
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
                        String paramType = paramType(constructor);
                        if (paramType != null) {
                            return new SourceArgsCookies(env, factoryType, type, constructor, samePackage, elements, paramType, name);
                        }
                    }
                }
                for (ExecutableElement method : findMethodByType(env, type, elements, samePackage)) {
                    String paramType = paramType(method);
                    if (paramType != null) {
                        return new SourceArgsCookies(env, factoryType, type, method, samePackage, elements, paramType, name);
                    }
                }
            } else {
                if (env.isAssignable(factoryType.asType(), type)) {
                    LinkedList<ExecutableElement> constructors = allConstructor(elements, samePackage);
                    ExecutableElement constructor = findConstructor(constructors, tag, samePackage);
                    if (constructor != null) {
                        String paramType = paramType(constructor);
                        if (paramType != null) {
                            return new SourceArgsCookies(env, factoryType, type, constructor, samePackage, elements, paramType, name);
                        }
                    }
                }
                ExecutableElement method = findMethodByTag(elements, tag, samePackage);
                if (method != null) {
                    String paramType = paramType(method);
                    if (paramType != null) {
                        return new SourceArgsCookies(env, factoryType, type, method, samePackage, elements, paramType, name);
                    }
                }
            }
        }
        env.error("No factory defend for type:" + type + " factory type:" + factoryClz);
        return SourceArgsFail.INSTANCE;
    }

    private static String paramType(ExecutableElement constructor) {
        List<? extends VariableElement> ps = constructor.getParameters();
        if (ps.size() == 1) {
            String paramType = ps.get(0).asType().toString();
            if (TextUtils.in(paramType, COOKIES_TYPES)) {
                return paramType;
            }
        }
        return null;
    }

    private SourceArgsCookies(ProcessEnvironment env,
                              TypeElement factoryType,
                              TypeMirror returnType,
                              ExecutableElement exec,
                              boolean samePackage,
                              List<? extends Element> elements,
                              String type,
                              String alias) {
        super(env, factoryType, exec, returnType);
        this.type = type;
        this.alias = alias;
        init(exec, samePackage, elements);
    }

    @Override
    protected void appendArgument(SourceServlet servlet, SourceArguments args, VariableElement param, StringBuilder b) {
        switch (type) {
            case T_Cookie -> b.append("_req.getCookie(\"").append(alias).append("\")");
            case T_Cookies -> b.append("_req.getCookies()");
        }
    }

    @Override
    public void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name) {
        args.addParam(p.asType().toString(), name, alias).write(toCharSequence(servlet, args, name));
    }

}

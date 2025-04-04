package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SourceCookieProcessor.SUPPORT_COOKIE_TYPE;
import static yeamy.restlite.annotation.SourceHeaderProcessor.SUPPORT_HEADER_TYPE;

abstract class SourceVariableHelper {

    private static ExecutableElement findInjectConstructor(List<? extends Element> list, boolean samePackage) {
        for (Element element : list) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) element;
                Set<Modifier> modifiers = constructor.getModifiers();
                if (modifiers.contains(Modifier.PUBLIC) || (samePackage && !modifiers.contains(Modifier.PRIVATE))) {
                    if (constructor.getParameters().isEmpty()) {
                        return constructor;
                    }
                }
            }
        }
        return null;
    }

    private static ArrayList<ExecutableElement> findInjectStaticMethod(ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
        ArrayList<ExecutableElement> methods = new ArrayList<>();
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            if (!method.getParameters().isEmpty()) continue;
            if (!env.isAssignable(returnType, method.getReturnType())) continue;
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            methods.add(method);
        }
        return methods;
    }

    public static SourceInject getInject(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Inject ann) {
        TypeMirror returnType = param.asType();
        String provider = ann.provider();
        SourceInjectProvider ip = env.getInjectProvider(returnType.toString(), provider);
        if (ip != null) {
            TypeElement classType = ip.importType;
            boolean samePackage = servlet.isSamePackage(classType);
            List<? extends Element> elements = param.getEnclosedElements();
            return new SourceInjectByProvider(env, param, ip, samePackage, elements);
        }
        if (TextUtils.isNotEmpty(provider)) {
            env.error("Cannot find InjectProvider with name: " + provider + " of type " + returnType);
            return new SourceInjectNull(env, param, returnType);
        }
        TypeElement classType = env.getTypeElement(returnType.toString());
        if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator without InjectProvider in abstract class: " + returnType);
            return new SourceInjectNull(env, param, returnType);
        }
        boolean samePackage = servlet.isSamePackage(classType);
        List<? extends Element> elements = param.getEnclosedElements();
        ExecutableElement exec = findInjectConstructor(elements, samePackage);
        if (exec != null) {
            return new SourceInjectByExecutable(env, param, classType, exec, returnType, samePackage, elements);
        }
        ArrayList<ExecutableElement> ms = findInjectStaticMethod(env, elements, returnType, samePackage);
        if (ms.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return new SourceInjectNull(env, param, returnType);
        } else if (ms.size() == 1) {
            exec = ms.get(0);
            return new SourceInjectByExecutable(env, param, classType, exec, returnType, samePackage, elements);
        } else {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return new SourceInjectNull(env, param, returnType);
        }
    }

    //----------------------------------------------

    private static void findHeaderConstructor(ArrayList<ExecutableElement> list, List<? extends Element> elements, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) continue;
            ExecutableElement constructor = (ExecutableElement) element;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            List<? extends VariableElement> parameters = constructor.getParameters();
            if (parameters.size() != 1) continue;
            if (TextUtils.in(parameters.get(0).asType().toString(), SUPPORT_HEADER_TYPE)) {
                list.add(constructor);
            }
        }
    }

    private static void findHeaderStaticMethod(ArrayList<ExecutableElement> list, ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            if (!env.isAssignable(returnType, method.getReturnType())) continue;
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() != 1) continue;
            if (TextUtils.in(parameters.get(0).asType().toString(), SUPPORT_HEADER_TYPE)) {
                list.add(method);
            }
        }
    }

    public static SourceHeader getHeader(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Header ann) {
        TypeMirror returnType = param.asType();
        String processor = ann.processor();
        SourceHeaderProcessor p = env.getHeaderProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            boolean samePackage = servlet.isSamePackage(p.classType);
            List<? extends Element> elements = param.getEnclosedElements();
            return new SourceHeaderByExecutable(env, param, p.classType, p.method, returnType, samePackage, elements);
        }
        if (TextUtils.isNotEmpty(processor)) {
            env.error("Cannot find HeaderProcessor with name: " + processor + " of type " + returnType);
            return null;
        }
        String returnTypeName = returnType.toString();
        if (TextUtils.in(returnTypeName, SUPPORT_HEADER_TYPE)) {
            return new SourceHeaderDefault(env, param, returnTypeName);
        }
        TypeElement classType = env.getTypeElement(returnTypeName);
        if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator without HeaderProcessor in abstract class: " + returnType);
            return null;
        }
        boolean samePackage = servlet.isSamePackage(classType);
        ArrayList<ExecutableElement> list = new ArrayList<>();
        List<? extends Element> elements = param.getEnclosedElements();
        findHeaderConstructor(list, elements, samePackage);
        if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        }
        findHeaderStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() == 1) {
            return new SourceHeaderByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
        } else if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        }
    }

    //----------------------------------------------
    private static void findCookieConstructor(ArrayList<ExecutableElement> list, List<? extends Element> elements, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) continue;
            ExecutableElement constructor = (ExecutableElement) element;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            List<? extends VariableElement> parameters = constructor.getParameters();
            if (parameters.size() != 1) continue;
            if (TextUtils.in(parameters.get(0).asType().toString(), SUPPORT_COOKIE_TYPE)) {
                list.add(constructor);
            }
        }
    }

    private static void findCookieStaticMethod(ArrayList<ExecutableElement> list, ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            if (!env.isAssignable(returnType, method.getReturnType())) continue;
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() != 1) continue;
            if (TextUtils.in(parameters.get(0).asType().toString(), SUPPORT_COOKIE_TYPE)) {
                list.add(method);
            }
        }
    }

    public static SourceCookie getCookie(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Cookies ann) {
        TypeMirror returnType = param.asType();
        String processor = ann.processor();
        SourceCookieProcessor p = env.getCookieProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            boolean samePackage = servlet.isSamePackage(p.classType);
            List<? extends Element> elements = param.getEnclosedElements();
            return new SourceCookieByExecutable(env, param, p.classType, p.method, returnType, samePackage, elements);
        }
        if (TextUtils.isNotEmpty(processor)) {
            env.error("Cannot find CookieProcessor with name: " + processor + " of type " + returnType);
            return null;
        }
        String returnTypeName = returnType.toString();
        if (TextUtils.in(returnTypeName, SUPPORT_COOKIE_TYPE)) {
            return new SourceCookieDefault(env, param, returnTypeName);
        }
        TypeElement classType = env.getTypeElement(returnTypeName);
        if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator without CookieProcessor in abstract class: " + returnType);
            return null;
        }
        boolean samePackage = servlet.isSamePackage(classType);
        List<? extends Element> elements = param.getEnclosedElements();
        ArrayList<ExecutableElement> list = new ArrayList<>();
        findCookieConstructor(list, elements, samePackage);
        if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        }
        findCookieStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() == 1) {
            return new SourceCookieByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
        } else if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        }
    }
}

package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class SourceHelper {

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
        List<? extends Element> elements = param.getEnclosedElements();
        SourceInjectProvider ip = env.getInjectProvider(returnType.toString(), provider);
        if (ip != null) {
            TypeElement classType = ip.importType;
            boolean samePackage = servlet.isSamePackage(classType);
            return new SourceInjectByProvider(env, param, ip, samePackage, elements);
        } else {
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
            ExecutableElement exec = findInjectConstructor(elements, samePackage);
            if (exec != null) {
                return new SourceInjectNoProvider(env, param, classType, exec, returnType, samePackage, elements);
            }
            ArrayList<ExecutableElement> ms = findInjectStaticMethod(env, elements, returnType, samePackage);
            if (ms.size() == 0) {
                env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
                return new SourceInjectNull(env, param, returnType);
            } else if (ms.size() > 1) {
                exec = ms.get(0);
                return new SourceInjectNoProvider(env, param, classType, exec, returnType, samePackage, elements);
            } else {
                env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
                return new SourceInjectNull(env, param, returnType);
            }
        }
    }

    private static ExecutableElement findHeaderConstructor(List<? extends Element> list, boolean samePackage) {
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

    private static ArrayList<ExecutableElement> findHeaderStaticMethod(ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
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

    public static SourceVariable getHeader(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Header ann) {TypeMirror returnType = param.asType();
        String processor = ann.processor();
        List<? extends Element> elements = param.getEnclosedElements();
        SourceHeaderProcessor hp = env.getHeaderProcessor(returnType.toString(), processor);
        if (hp != null) {
            TypeElement classType = hp.importType;
            boolean samePackage = servlet.isSamePackage(classType);
            return new SourceHeaderByProcessor(env, param, hp, samePackage, elements);
        } else {
            if (TextUtils.isNotEmpty(processor)) {
                env.error("Cannot find HeaderProcessor with name: " + processor + " of type " + returnType);
                return new SourceInjectNull(env, param, returnType);
            }
            TypeElement classType = env.getTypeElement(returnType.toString());
            if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
                env.error("Cannot find creator without HeaderProcessor in abstract class: " + returnType);
                return new SourceInjectNull(env, param, returnType);
            }
            boolean samePackage = servlet.isSamePackage(classType);
            ExecutableElement exec = findHeaderConstructor(elements, samePackage);
            if (exec != null) {
                return new SourceHeaderNoProcessor(env, param, classType, exec, returnType, samePackage, elements);
            }
            ArrayList<ExecutableElement> ms = findHeaderStaticMethod(env, elements, returnType, samePackage);
            if (ms.size() == 0) {
                env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
                return new SourceInjectNull(env, param, returnType);
            } else if (ms.size() > 1) {
                exec = ms.get(0);
                return new SourceHeaderNoProcessor(env, param, classType, exec, returnType, samePackage, elements);
            } else {
                env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
                return new SourceInjectNull(env, param, returnType);
            }
        }
    }
}

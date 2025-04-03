package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

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
    }

    //----------------------------------------------
    private static final String[] SUPPORT_HEADER_TYPE = new String[]{T_String, T_int, T_Integer, T_long, T_Long, T_Date};

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
        List<? extends Element> elements = param.getEnclosedElements();
        SourceHeaderProcessor hp = env.getHeaderProcessor(returnType.toString(), processor);
        if (hp != null) {
            TypeElement classType = hp.importType;
            boolean samePackage = servlet.isSamePackage(classType);
            return new SourceHeaderByProcessor(env, param, hp, samePackage, elements);
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
}

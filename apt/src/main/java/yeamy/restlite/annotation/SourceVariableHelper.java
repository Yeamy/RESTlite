package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SourceBodyProcessor.SUPPORT_BODY_TYPE;
import static yeamy.restlite.annotation.SourceCookieProcessor.SUPPORT_COOKIE_TYPE;
import static yeamy.restlite.annotation.SourceHeaderProcessor.SUPPORT_HEADER_TYPE;
import static yeamy.restlite.annotation.SourceParamProcessor.SUPPORT_PARAM_TYPE;
import static yeamy.restlite.annotation.SourcePartProcessor.SUPPORT_PART_TYPE;

abstract class SourceVariableHelper {

    private static void findInjectConstructor(ArrayList<ExecutableElement> list, List<? extends Element> elements, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) continue;
            ExecutableElement constructor = (ExecutableElement) element;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC) || (samePackage && !modifiers.contains(Modifier.PRIVATE))) {
                if (constructor.getParameters().isEmpty()) {
                    list.add(constructor);
                }
            }
        }
    }

    private static void findInjectStaticMethod(ArrayList<ExecutableElement> list, ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            if (!method.getParameters().isEmpty()) continue;
            if (!env.isAssignable(returnType, method.getReturnType())) continue;
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            list.add(method);
        }
    }

    public static SourceInject getInject(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Inject ann) {
        TypeMirror returnType = param.asType();
        String provider = ann.provider();
        SourceInjectProvider ip = env.getInjectProvider(returnType.toString(), provider);
        if (ip != null) {
            return new SourceInjectByProvider(env, param, ip);
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
        List<? extends Element> elements = classType.getEnclosedElements();
        ArrayList<ExecutableElement> list = new ArrayList<>();
        findInjectStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return new SourceInjectNull(env, param, returnType);
        } else if (list.size() == 0) {
            findInjectConstructor(list, elements, samePackage);
        }
        if (list.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType
                    + " " + param.getSimpleName());
            return new SourceInjectNull(env, param, returnType);
        } else if (list.size() > 1) {
            env.error("More than one constructor in type:" + returnType + " " + param.getSimpleName());
            return new SourceInjectNull(env, param, returnType);
        } else {
            return new SourceInjectByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
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
            return new SourceHeaderByExecutable(env, param, p);
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
        List<? extends Element> elements = classType.getEnclosedElements();
        findHeaderStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else if (list.size() == 0) {
            findHeaderConstructor(list, elements, samePackage);
        }
        if (list.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        } else if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            return new SourceHeaderByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
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
            return new SourceCookieByExecutable(env, param, p);
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
        List<? extends Element> elements = classType.getEnclosedElements();
        ArrayList<ExecutableElement> list = new ArrayList<>();
        findCookieStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else if (list.size() == 0) {
            findCookieConstructor(list, elements, samePackage);
        }
        if (list.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        } else if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            return new SourceCookieByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
        }
    }

    public static SourceCookieByExecutable getCookieByFactory(ProcessEnvironment env, VariableElement param, CookieFactoryBean bean) {
        TypeMirror returnType = param.asType();
        String processor = bean.ann().processor();
        SourceCookieProcessor p = env.getCookieProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceCookieByExecutable(env, param, p);
        }
        String factoryClz = ProcessEnvironment.getClassInAnnotation(bean.ann()::processorClass);
        TypeElement classType = env.getTypeElement(factoryClz);
        List<? extends Element> elements = classType.getEnclosedElements();
        for (Element element : elements) {
            if (element instanceof ExecutableElement e) {
                CookieProcessor pn = e.getAnnotation(CookieProcessor.class);
                if (pn != null && pn.value().equals(processor)) {
                    env.addCookieProcessor(e, pn);
                }
            }
        }
        p = env.getCookieProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceCookieByExecutable(env, param, p);
        }
        return null;
    }

    //----------------------------------------------

    private static void findBodyConstructor(ArrayList<ExecutableElement> list, List<? extends Element> elements, TypeMirror type, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) continue;
            ExecutableElement constructor = (ExecutableElement) element;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            if (!SourceBodyProcessor.checkParam(type, constructor.getParameters())) continue;
            list.add(constructor);
        }
    }

    private static void findBodyStaticMethod(ArrayList<ExecutableElement> list, ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            TypeMirror mrt = method.getReturnType();
            if (!env.isAssignableVar(returnType, mrt)) continue;
            if (!SourceBodyProcessor.checkParam(returnType, method.getParameters())) continue;
            list.add(method);
        }
    }

    public static SourceBody getBody(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Body ann) {
        TypeMirror returnType = param.asType();
        String processor = ann.processor();
        SourceBodyProcessor p = env.getBodyProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceBodyByExecutable(env, param, p);
        }
        if (TextUtils.isNotEmpty(processor)) {
            env.error("Cannot find BodyProcessor with name: " + processor + " of type " + returnType);
            return null;
        }
        String returnTypeName = returnType.toString();
        if (TextUtils.in(returnTypeName, SUPPORT_BODY_TYPE)) {
            return new SourceBodyDefault(env, param, returnTypeName);
        }
        TypeElement classType = env.getTypeElement(returnTypeName);
        if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator without BodyProcessor in abstract class: " + returnType);
            return null;
        }
        boolean samePackage = servlet.isSamePackage(classType);
        List<? extends Element> elements = classType.getEnclosedElements();
        ArrayList<ExecutableElement> list = new ArrayList<>();
        findBodyStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else if (list.size() == 0) {
            findBodyConstructor(list, elements, returnType, samePackage);
        }
        if (list.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        } else if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            return new SourceBodyByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
        }
    }

    public static SourceBody getBody(ProcessEnvironment env, VariableElement param, BodyFactory ann) {
        TypeMirror returnType = param.asType();
        String processor = ann.processor();
        SourceBodyProcessor p = env.getBodyProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceBodyByExecutable(env, param, p);
        }
        String factoryClz = ProcessEnvironment.getClassInAnnotation(ann::processorClass);
        TypeElement classType = env.getTypeElement(factoryClz);
        List<? extends Element> elements = classType.getEnclosedElements();
        for (Element element : elements) {
            if (element instanceof ExecutableElement e) {
                BodyProcessor pn = e.getAnnotation(BodyProcessor.class);
                if (pn != null && pn.value().equals(processor)) {
                    env.addBodyProcessor(e, pn);
                }
            }
        }
        p = env.getBodyProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceBodyByExecutable(env, param, p);
        }
        return null;
    }

    //----------------------------------------------

    private static void findPartConstructor(ArrayList<ExecutableElement> list, List<? extends Element> elements, TypeMirror type, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) continue;
            ExecutableElement constructor = (ExecutableElement) element;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            if (!SourcePartProcessor.checkParam(type, constructor.getParameters())) continue;
            list.add(constructor);
        }
    }

    private static void findPartStaticMethod(ArrayList<ExecutableElement> list, ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC)) continue;
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            TypeMirror mrt = method.getReturnType();
            if (!env.isAssignableVar(returnType, mrt)) continue;
            if (!SourcePartProcessor.checkParam(returnType, method.getParameters())) continue;
            list.add(method);
        }
    }

    public static SourcePart getPart(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Parts ann) {
        TypeMirror returnType = param.asType();
        String processor = ann.processor();
        SourcePartProcessor p = env.getPartProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourcePartByExecutable(env, param, p);
        }
        if (TextUtils.isNotEmpty(processor)) {
            env.error("Cannot find PartProcessor with name: " + processor + " of type " + returnType);
            return null;
        }
        String returnTypeName = returnType.toString();
        if (TextUtils.in(returnTypeName, SUPPORT_PART_TYPE)) {
            return new SourcePartDefault(env, param, returnTypeName);
        }
        TypeElement classType = env.getTypeElement(returnTypeName);
        if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator without PartProcessor in abstract class: " + returnType);
            return null;
        }
        boolean samePackage = servlet.isSamePackage(classType);
        List<? extends Element> elements = classType.getEnclosedElements();
        ArrayList<ExecutableElement> list = new ArrayList<>();
        findPartStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else if (list.size() == 0) {
            findPartConstructor(list, elements, returnType, samePackage);
        }
        if (list.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        } else if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            return new SourcePartByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
        }
    }

    public static SourcePartByExecutable getPartByFactory(ProcessEnvironment env, VariableElement param, PartFactoryBean bean) {
        TypeMirror returnType = param.asType();
        String processor = bean.ann().processor();
        SourcePartProcessor p = env.getPartProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourcePartByExecutable(env, param, p);
        }
        String factoryClz = ProcessEnvironment.getClassInAnnotation(bean.ann()::processorClass);
        TypeElement classType = env.getTypeElement(factoryClz);
        List<? extends Element> elements = classType.getEnclosedElements();
        for (Element element : elements) {
            if (element instanceof ExecutableElement e) {
                PartProcessor pn = e.getAnnotation(PartProcessor.class);
                if (pn != null && pn.value().equals(processor)) {
                    env.addPartProcessor(e, pn);
                }
            }
        }
        p = env.getPartProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourcePartByExecutable(env, param, p);
        }
        return null;
    }

    //----------------------------------------------

    private static void findParamConstructor(ArrayList<ExecutableElement> list, List<? extends Element> elements, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CONSTRUCTOR) continue;
            ExecutableElement constructor = (ExecutableElement) element;
            Set<Modifier> modifiers = constructor.getModifiers();
            if (modifiers.contains(Modifier.PRIVATE)) continue;
            if (!samePackage && !modifiers.contains(Modifier.PUBLIC)) continue;
            List<? extends VariableElement> parameters = constructor.getParameters();
            if (parameters.size() != 1) continue;
            if (TextUtils.in(parameters.get(0).asType().toString(), SUPPORT_PARAM_TYPE)) {
                list.add(constructor);
            }
        }
    }

    private static void findParamStaticMethod(ArrayList<ExecutableElement> list, ProcessEnvironment env, List<? extends Element> elements, TypeMirror returnType, boolean samePackage) {
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
            if (TextUtils.in(parameters.get(0).asType().toString(), SUPPORT_PARAM_TYPE)) {
                list.add(method);
            }
        }
    }

    public static SourceParam getParam(ProcessEnvironment env, SourceServlet servlet, VariableElement param, Param ann) {
        TypeMirror returnType = param.asType();
        String processor = ann.processor();
        SourceParamProcessor p = env.getParamProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceParamByExecutable(env, param, p);
        }
        if (TextUtils.isNotEmpty(processor)) {
            env.error("Cannot find ParamProcessor with name: " + processor + " of type " + returnType);
            return null;
        }
        String returnTypeName = returnType.toString();
        if (TextUtils.in(returnTypeName, SUPPORT_PARAM_TYPE)) {
            return new SourceParamDefault(env, param, returnTypeName);
        }
        TypeElement classType = env.getTypeElement(returnTypeName);
        if (classType.getModifiers().contains(Modifier.ABSTRACT)) {
            env.error("Cannot find creator without ParamProcessor in abstract class: " + returnType);
            return null;
        }
        boolean samePackage = servlet.isSamePackage(classType);
        ArrayList<ExecutableElement> list = new ArrayList<>();
        List<? extends Element> elements = classType.getEnclosedElements();
        findParamStaticMethod(list, env, elements, returnType, samePackage);
        if (list.size() > 1) {
            env.error("More than one Constructor in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else if (list.size() == 0) {
            findParamConstructor(list, elements, samePackage);
        }
        if (list.size() == 0) {
            env.error("Cannot find Constructor nor Static-Factory-Method with no argument in type " + returnType);
            return null;
        } else if (list.size() > 1) {
            env.error("More than one Static-Factory-Method in type:" + returnType + " " + param.getSimpleName());
            return null;
        } else {
            return new SourceParamByExecutable(env, param, classType, list.get(0), returnType, samePackage, elements);
        }
    }

    public static SourceParamByExecutable getParamByFactory(ProcessEnvironment env, VariableElement param, ParamFactoryBean bean) {
        TypeMirror returnType = param.asType();
        String processor = bean.ann().processor();
        SourceParamProcessor p = env.getParamProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceParamByExecutable(env, param, p);
        }
        String factoryClz = ProcessEnvironment.getClassInAnnotation(bean.ann()::processorClass);
        TypeElement classType = env.getTypeElement(factoryClz);
        List<? extends Element> elements = classType.getEnclosedElements();
        for (Element element : elements) {
            if (element instanceof ExecutableElement e) {
                ParamProcessor pn = e.getAnnotation(ParamProcessor.class);
                if (pn != null && pn.value().equals(processor)) {
                    env.addParamProcessor(e, pn);
                }
            }
        }
        p = env.getParamProcessor(returnType.toString(), processor);
        if (p != null) {
            if (p.method == null) return null;
            return new SourceParamByExecutable(env, param, p);
        }
        return null;
    }
}

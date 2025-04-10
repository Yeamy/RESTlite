package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.Callable;

class ProcessEnvironment {
    final ProcessingEnvironment processingEnv;
    private final Messager messager;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final boolean responseAllType;
    private final String charset;
    private final String pkg, response;
    private final TypeMirror CLOSEABLE, HTTP_RESPONSE, INPUT_STREAM, FILE;
    private final TreeMap<String, Map<String, String>> implMethodNames = new TreeMap<>();
    private final ProcessorMap<SourceInjectProvider> injectProviders = new ProcessorMap<>();
    private final ProcessorMap<SourceHeaderProcessor> headerProcessors = new ProcessorMap<>();
    private final ProcessorMap<SourceCookieProcessor> cookieProcessors = new ProcessorMap<>();
    private final ProcessorMap<SourceBodyProcessor> bodyProcessors = new ProcessorMap<>();
    private final ProcessorMap<SourcePartProcessor> partProcessors = new ProcessorMap<>();
    private final ProcessorMap<SourceParamProcessor> paramProcessors = new ProcessorMap<>();

    public ProcessEnvironment(ProcessingEnvironment env, Element init) {
        processingEnv = env;
        messager = env.getMessager();
        typeUtils = env.getTypeUtils();
        elementUtils = env.getElementUtils();
        PackageElement element = (PackageElement) init.getEnclosingElement();
        pkg = element.getQualifiedName().toString();
        Configuration ann = init.getAnnotation(Configuration.class);
        responseAllType = ann.responseAllType();
        charset = ann.charset();
        this.response = getClassInAnnotation(ann::response);
        //
        CLOSEABLE = elementUtils.getTypeElement("java.io.Closeable").asType();
        HTTP_RESPONSE = elementUtils.getTypeElement("yeamy.restlite.HttpResponse").asType();
        INPUT_STREAM = elementUtils.getTypeElement("java.io.InputStream").asType();
        FILE = elementUtils.getTypeElement("java.io.File").asType();
    }

    public boolean isAssignable(TypeMirror subType, TypeMirror type) {
        return typeUtils.isAssignable(subType, type);
    }

    public boolean isAssignableVar(TypeMirror subType, TypeMirror type) {
        if (type.getKind().equals(TypeKind.TYPEVAR)) {
            Element e = asElement(type);
            if (e instanceof TypeParameterElement ee) {
                List<? extends TypeMirror> bounds = ee.getBounds();
                for (TypeMirror bound : bounds) {
                    if (typeUtils.isAssignable(subType, bound)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return typeUtils.isAssignable(subType, type);
    }

    public Element asElement(TypeMirror t) {
        return typeUtils.asElement(t);
    }

    public boolean isCloseable(TypeMirror t) {
        return typeUtils.isSubtype(t, CLOSEABLE);
    }

    public static boolean isCloseThrow(TypeElement type) {
        for (Element element : type.getEnclosedElements()) { // find close method
            if (element.getKind() != ElementKind.METHOD) continue;//
            if (!element.getSimpleName().toString().equals("close")) continue;
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) continue;
            if (!modifiers.contains(Modifier.PUBLIC)) continue;
            ExecutableElement close = (ExecutableElement) element;
            if (close.getParameters().size() != 0) continue;
            return close.getThrownTypes().size() > 0;
        }
        return false;
    }

    public boolean isHttpResponse(TypeMirror t) {
        return typeUtils.isSubtype(t, HTTP_RESPONSE);
    }

    public boolean isStream(TypeMirror t) {
        return typeUtils.isSubtype(t, INPUT_STREAM) || typeUtils.isSameType(t, INPUT_STREAM)//
                || typeUtils.isSameType(t, FILE) || typeUtils.isSubtype(t, FILE);
    }

    public TypeElement getTypeElement(String clz) {
        return elementUtils.getTypeElement(clz);
    }

    public static BodyFactory getBodyFactory(VariableElement param) {
        for (AnnotationMirror am : param.getAnnotationMirrors()) {
            BodyFactory body = am.getAnnotationType().asElement().getAnnotation(BodyFactory.class);
            if (body != null) {
                return body;
            }
        }
        return null;
    }

    public static SourceFactory<CookieFactory> getCookieFactory(VariableElement param) {
        for (AnnotationMirror am : param.getAnnotationMirrors()) {
            CookieFactory ann = am.getAnnotationType().asElement().getAnnotation(CookieFactory.class);
            if (ann == null) continue;
            String method = ann.nameMethod();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(method)) {
                    return new SourceFactory<>(ann, entry.getValue().getValue().toString());
                }
            }
        }
        return null;
    }

    public static SourceFactory<PartFactory> getPartFactory(VariableElement param) {
        for (AnnotationMirror am : param.getAnnotationMirrors()) {
            PartFactory ann = am.getAnnotationType().asElement().getAnnotation(PartFactory.class);
            if (ann == null) continue;
            String method = ann.nameMethod();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(method)) {
                    return new SourceFactory<>(ann, entry.getValue().getValue().toString());
                }
            }
        }
        return null;
    }

    public static SourceFactory<ParamFactory> getParamFactory(VariableElement param) {
        for (AnnotationMirror am : param.getAnnotationMirrors()) {
            ParamFactory ann = am.getAnnotationType().asElement().getAnnotation(ParamFactory.class);
            if (ann == null) continue;
            String method = ann.nameMethod();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(method)) {
                    return new SourceFactory<>(ann, entry.getValue().getValue().toString());
                }
            }
        }
        return null;
    }

    public String getFileName(String pkg, String name) {
        int i = 0;
        String name2 = name;
        while (true) {
            Element e = elementUtils.getTypeElement(pkg + '.' + name2);
            if (e == null) {
                return name2;
            }
            name2 = name + i++;
        }
    }

    public String charset(String... charset) {
        for (String c : charset) {
            if (TextUtils.isNotEmpty(c)) {
                return c;
            }
        }
        return this.charset;
    }

    public String getPackage() {
        return pkg;
    }

    public String getResponse() {
        return response;
    }

    public void error(String msg) {
        messager.printMessage(Kind.ERROR, msg);
    }

    public void error(Exception e) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(os)) {
            e.printStackTrace(ps);
            messager.printMessage(Kind.ERROR, os.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void warning(String msg) {
        messager.printMessage(Kind.WARNING, msg);
    }

    public void note(String msg) {
        messager.printMessage(Kind.NOTE, msg);
    }

    public String addImplMethod(String httpMethod, SourceImplMethodName methodName) {
        String key = methodName.resource + ':' + httpMethod;
        Map<String, String> value = implMethodNames.computeIfAbsent(key, k -> new TreeMap<>(Comparator.reverseOrder()));
        String name = methodName.getName(httpMethod);
        value.put(name, methodName.ifHas);
        return name;
    }

    public TreeMap<String, Map<String, String>> implMethodNames() {
        return implMethodNames;
    }

    public void addHeaderProcessor(Element element, HeaderProcessor ann) {
        headerProcessors.add(element, ann.value(), new SourceHeaderProcessor(this, element));
    }

    public SourceHeaderProcessor getHeaderProcessor(String type, String name) {
        return headerProcessors.get(type, name);
    }

    public void addCookieProcessor(Element element, CookieProcessor ann) {
        cookieProcessors.add(element, ann.value(), new SourceCookieProcessor(this, element));
    }

    public SourceCookieProcessor getCookieProcessor(String type, String name) {
        return cookieProcessors.get(type, name);
    }

    public void addBodyProcessor(Element element, BodyProcessor ann) {
        bodyProcessors.add(element, ann.value(), new SourceBodyProcessor(this, element));
    }

    public SourceBodyProcessor getBodyProcessor(String type, String name) {
        return bodyProcessors.get(type, name);
    }

    public void addPartProcessor(Element element, PartProcessor ann) {
        partProcessors.add(element, ann.value(), new SourcePartProcessor(this, element));
    }

    public SourcePartProcessor getPartProcessor(String type, String name) {
        return partProcessors.get(type, name);
    }

    public void addParamProcessor(Element element, ParamProcessor ann) {
        paramProcessors.add(element, ann.value(), new SourceParamProcessor(this, element));
    }

    public SourceParamProcessor getParamProcessor(String type, String name) {
        return paramProcessors.get(type, name);
    }

    public void addInjectProvider(Element element, InjectProvider ann) {
        SourceInjectProvider p = new SourceInjectProvider(this, element);
        injectProviders.add(element, ann.value(), p);
        if (p.method == null) return;
        try {
            for (Class<?> clz : ann.provideFor()) {
                String type = clz.getName();
                if (isAssignable(p.outType, getTypeElement(type).asType())) {
                    injectProviders.add(type, ann.value(), p);
                }
            }
        } catch (MirroredTypesException e) {
            List<? extends TypeMirror> tms = e.getTypeMirrors();
            tms.forEach(tm -> {
                if (isAssignable(p.outType, tm)) {
                    injectProviders.add(tm.toString(), ann.value(), p);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SourceInjectProvider getInjectProvider(String type, String name) {
        return injectProviders.get(type, name);
    }

    public boolean responseAllType() {
        return responseAllType;
    }

    public static String getClassInAnnotation(Callable<Class<?>> runnable) {
        try {
            return runnable.call().getName();
        } catch (MirroredTypeException e) {
            TypeMirror tm = e.getTypeMirror();
            if (tm.getKind().equals(TypeKind.VOID)) {
                return "";
            }
            return tm.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

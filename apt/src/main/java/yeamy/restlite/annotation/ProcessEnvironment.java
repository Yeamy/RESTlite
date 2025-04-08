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
import javax.tools.Diagnostic;
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
    private final TypeMirror closeable, httpResponse, inputStream, file;
    final TreeMap<String, Map<String, String>> names = new TreeMap<>();
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
        closeable = elementUtils.getTypeElement("java.io.Closeable").asType();
        httpResponse = elementUtils.getTypeElement("yeamy.restlite.HttpResponse").asType();
        inputStream = elementUtils.getTypeElement("java.io.InputStream").asType();
        file = elementUtils.getTypeElement("java.io.File").asType();
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

    public static boolean isThrowable(ExecutableElement element) {
        return element.getThrownTypes().size() > 0;
    }

    public boolean isCloseable(TypeMirror t) {
        return typeUtils.isSubtype(t, closeable);
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
        return typeUtils.isSubtype(t, httpResponse);
    }

    public boolean isStream(TypeMirror t) {
        return typeUtils.isSubtype(t, inputStream) || typeUtils.isSameType(t, inputStream)//
                || typeUtils.isSameType(t, file) || typeUtils.isSubtype(t, file);
    }

    public TypeElement getTypeElement(String clz) {
        return elementUtils.getTypeElement(clz);
    }

    public static BodyFactory getBodyFactory(VariableElement e) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            BodyFactory body = am.getAnnotationType().asElement().getAnnotation(BodyFactory.class);
            if (body != null) {
                return body;
            }
        }
        return null;
    }

    public static PartFactory getPartFactory(VariableElement e) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            PartFactory part = am.getAnnotationType().asElement().getAnnotation(PartFactory.class);
            if (part != null) {
                return part;
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
            messager.printMessage(Diagnostic.Kind.ERROR, os.toString());
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

    public String addServerName(String httpMethod, SourceServiceName serverName) {
        String key = serverName.resource + ':' + httpMethod;
        Map<String, String> value = names.computeIfAbsent(key, k -> new TreeMap<>(Comparator.reverseOrder()));
        String key2 = serverName.getName(httpMethod);
        value.put(key2, serverName.ifHas);
        return key2;
    }

    public Iterable<? extends Map.Entry<String, Map<String, String>>> serverNames() {
        return names.entrySet();
    }

    public void addSourceHeaderProcessor(Element element, HeaderProcessor ann) {
        headerProcessors.add(element, ann.value(), new SourceHeaderProcessor(this, element));
    }

    public SourceHeaderProcessor getHeaderProcessor(String type, String name) {
        return headerProcessors.get(type, name);
    }

    public void addSourceCookieProcessor(Element element, CookieProcessor ann) {
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

package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
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
    private final HashMap<String, SourceArgs> paramCreator = new HashMap<>();
    private final boolean responseAllType;
    private final String charset;
    private final SupportPatch supportPatch;
    private final String pkg, response;
    private final TypeMirror closeable, httpResponse, inputStream, file;
    final TreeMap<String, Map<String, String>> names = new TreeMap<>();
    private final HashMap<String, SourceInjectProvider> injectProviders = new HashMap<>();

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
        supportPatch = ann.supportPatch();
        this.response = getAnnotationType(ann::response);
        //
        closeable = elementUtils.getTypeElement("java.io.Closeable").asType();
        httpResponse = elementUtils.getTypeElement("yeamy.restlite.HttpResponse").asType();
        inputStream = elementUtils.getTypeElement("java.io.InputStream").asType();
        file = elementUtils.getTypeElement("java.io.File").asType();
    }

    public boolean isAssignable(TypeMirror subType, TypeMirror type) {
        return typeUtils.isAssignable(subType, type);
    }

    public Element asElement(TypeMirror t) {
        return typeUtils.asElement(t);
    }

    public boolean isCloseable(TypeMirror t) {
        return typeUtils.isSubtype(t, closeable);
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

    public static Body getBody(VariableElement e) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            Body body = am.getAnnotationType().asElement().getAnnotation(Body.class);
            if (body != null) {
                return body;
            }
        }
        return null;
    }

    public static Part getPart(VariableElement e) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            Part part = am.getAnnotationType().asElement().getAnnotation(Part.class);
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

    public SupportPatch supportPatch() {
        return supportPatch;
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

    public void addInjectProvider(Element element, InjectProvider ann) {
        String name = ann.value();
        ElementKind kind = element.getKind();
        ArrayList<String> keys = new ArrayList<>();
        for (Class<?> t : ann.provideFor()) {
            keys.add(t.getName());
        }
        if (kind == ElementKind.FIELD) {
            keys.add(element.asType().toString());
        } else {
            keys.add(((ExecutableElement) element).getReturnType().toString());
        }
        SourceInjectProvider provider = new SourceInjectProvider(this, element, name);
        for (String key : keys) {
            injectProviders.put(key, provider);
        }
        if (TextUtils.isNotEmpty(name)) {
            for (String key : keys) {
                injectProviders.put(key + ":" + name, provider);
            }
        }
    }

    public SourceInjectProvider getInjectProvider(String type, String name) {
        return injectProviders.get(TextUtils.isEmpty(name) ? type : type + ":" + name);
    }

    public boolean responseAllType() {
        return responseAllType;
    }

    public static String getAnnotationType(Callable<Class<?>> runnable) {
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

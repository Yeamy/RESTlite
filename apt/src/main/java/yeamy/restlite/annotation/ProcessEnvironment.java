package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class ProcessEnvironment {
    final ProcessingEnvironment processingEnv;
    private final Messager messager;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final HashMap<String, SourceParam> paramCreator = new HashMap<>();
    private final boolean responseAllType;
    private final String charset;
    private final SupportPatch supportPatch;
    private final String pkg, response;
    private final TypeMirror closeable, httpResponse, inputStream, file;
    final TreeMap<String, Map<String, String>> names = new TreeMap<>();
    private final HashMap<String, SourceInjectProvider> injects = new HashMap<>();

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
        this.response = response(ann);
        //
        closeable = elementUtils.getTypeElement("java.io.Closeable").asType();
        httpResponse = elementUtils.getTypeElement("yeamy.restlite.HttpResponse").asType();
        inputStream = elementUtils.getTypeElement("java.io.InputStream").asType();
        file = elementUtils.getTypeElement("java.io.File").asType();
    }

    private String response(Configuration ann) {
        try {
            Class<?> t = ann.response();
            return elementUtils.getTypeElement(t.getName()).getQualifiedName().toString();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror().toString();
        }
    }

    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return typeUtils.isAssignable(t1, t2);
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

    public SourceParam getBodyCreator(SourceServlet servlet, TypeMirror t, Body body) {
        String className = creator(body);
        String tag = body.tag();
        TypeElement te = getTypeElement(t.toString());
        boolean samePackage = false;
        if (!t.getKind().isPrimitive()) {
            String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
            samePackage = fpk.equals(servlet.pkg);
        }
        if (className.length() == 0) {
            body = te.getAnnotation(Body.class);
            if (body == null) {
                String id = "bc:" + te.getQualifiedName() + ":" + tag;
                SourceParam arg = paramCreator.get(id);
                if (arg == null) {
                    arg = SourceParamConstructor.body(this, samePackage, te, tag);
                    paramCreator.put(id, arg);
                }
                return arg;
            }
            String creator2 = creator(body);
            className = creator2.length() > 0 ? creator2 : t.toString();
        }
        if (tag.length() > 0) {// by tag
            String id = "bt:" + className + ":" + tag;
            SourceParam creator = paramCreator.get(id);
            if (creator == null) {
                creator = SourceParamFactory.body(this, samePackage, className, t, tag);
                paramCreator.put(id, creator);
            }
            return creator;
        } else {// by type
            String id = "bf:" + className + ":" + t + ":" + (samePackage ? "" : servlet.pkg);
            SourceParam creator = paramCreator.get(id);
            if (creator == null) {
                creator = SourceParamFactory.body(this, samePackage, className, t);
                paramCreator.put(id, creator);
            }
            return creator;
        }
    }

    public SourceParam getInjectParam(SourceServlet servlet, VariableElement param, Inject inject) {
        if (inject.singleton().equals(Singleton.yes)) {
            return new SourceParamInject(this, param);
        }
        TypeMirror t = param.asType();
        String className = creator(inject);
        String tag = inject.tag();
        TypeElement te = getTypeElement(t.toString());
        boolean samePackage = false;
        if (!t.getKind().isPrimitive()) {
            String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
            samePackage = fpk.equals(servlet.pkg);
        }
        if (className.length() == 0) {
            inject = te.getAnnotation(Inject.class);
            if (inject == null) {
                String id = "ic:" + te.getQualifiedName() + ":" + tag;
                SourceParam creator = paramCreator.get(id);
                if (creator == null) {
                    creator = SourceParamConstructor.inject(this, samePackage, te, tag);
                    paramCreator.put(id, creator);
                }
                return creator;
            }
            String creator2 = creator(inject);
            className = creator2.length() > 0 ? creator2 : t.toString();
        }
        if (tag.length() > 0) {// by tag
            String id = "it:" + className + ":" + tag;
            SourceParam creator = paramCreator.get(id);
            if (creator == null) {
                creator = SourceParamFactory.inject(this, samePackage, className, t, tag);
                paramCreator.put(id, creator);
            }
            return creator;
        } else {// by type
            String id = "if:" + className + ":" + t + ":" + (samePackage ? "" : servlet.pkg);
            SourceParam creator = paramCreator.get(id);
            if (creator == null) {
                creator = SourceParamFactory.inject(this, samePackage, className, t);
                paramCreator.put(id, creator);
            }
            return creator;
        }
    }

    public String creator(Body body) {
        try {
            Class<?> clz = body.creator();
            if (clz.equals(void.class)) {
                return "";
            }
            return elementUtils.getTypeElement(clz.getName()).getQualifiedName().toString();
        } catch (MirroredTypeException e) {
            String t = e.getTypeMirror().toString();
            if ("void".equals(t)) {
                return "";
            }
            return t;
        }
    }

    public String creator(Inject inject) {
        try {
            Class<?> clz = inject.creator();
            if (clz.equals(void.class)) {
                return "";
            }
            return elementUtils.getTypeElement(clz.getName()).getQualifiedName().toString();
        } catch (MirroredTypeException e) {
            String t = e.getTypeMirror().toString();
            if ("void".equals(t)) {
                return "";
            }
            return t;
        }
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

    public String charset(String charset) {
        if (TextUtils.isEmpty(charset)) {
            return this.charset;
        }
        return charset;
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

    public void addInject(SourceInjectProvider inject) {
        for(String type : inject.types) {
            injects.put(type, inject);
        }
    }

    public SourceInjectProvider getInject(String type) {
        return injects.get(type);
    }

    public boolean responseAllType() {
        return responseAllType;
    }

}

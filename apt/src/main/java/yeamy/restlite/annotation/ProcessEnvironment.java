package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class ProcessEnvironment {
    private final Messager messager;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Filer filer;
    private final HashMap<String, SourceParamCreator> paramCreator = new HashMap<>();
    private final String charset;
    private final SupportPatch supportPatch;
    private final String pkg, response;
    private final TypeMirror closeable, httpResponse, inputStream, file;
    final TreeMap<String, Map<String, String>> names = new TreeMap<>();
    private final HashMap<String, SourceInjectProvider> injects = new HashMap<>();

    public ProcessEnvironment(ProcessingEnvironment env, TypeElement init) {
        messager = env.getMessager();
        typeUtils = env.getTypeUtils();
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
        PackageElement element = (PackageElement) init.getEnclosingElement();
        pkg = element.getQualifiedName().toString();
        Configuration ann = init.getAnnotation(Configuration.class);
        charset = ann.charset();
        supportPatch = ann.supportPatch();
        response = ann.response();
        //
        closeable = elementUtils.getTypeElement("java.io.Closeable").asType();
        httpResponse = elementUtils.getTypeElement("yeamy.restlite.HttpResponse").asType();
        inputStream = elementUtils.getTypeElement("java.io.InputStream").asType();
        file = elementUtils.getTypeElement("java.io.File").asType();
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

    public SourceParamCreator getBodyCreator(SourceServlet servlet, TypeMirror t, Body body) {
        String className = body.creator();
        String tag = body.tag();
        TypeElement te = getTypeElement(t.toString());
        boolean samePackage = false;
        if (!t.getKind().isPrimitive()) {
            String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
            samePackage = fpk.equals(servlet.getPackage());
        }
        if (className.length() == 0) {
            Body a = te.getAnnotation(Body.class);
            if (a == null) {
                String id = "c:" + te.getQualifiedName() + ":" + tag;
                SourceParamCreator arg = paramCreator.get(id);
                if (arg == null) {
                    arg = SourceParamConstructor.body(this, samePackage, te, tag);
                    paramCreator.put(id, arg);
                }
                return arg;
            }
            className = a.creator().length() > 0 ? a.creator() : t.toString();
        }
        if (tag.length() > 0) {// by tag
            String id = "t:" + className + ":" + tag;
            SourceParamCreator creator = paramCreator.get(id);
            if (creator == null) {
                creator = SourceParamFactory.body(this, samePackage, className, t, tag);
                paramCreator.put(id, creator);
            }
            return creator;
        } else {// by type
            String id = "f:" + className + ":" + t + ":" + (samePackage ? "" : servlet.getPackage());
            SourceParamCreator creator = paramCreator.get(id);
            if (creator == null) {
                creator = SourceParamFactory.body(this, samePackage, className, t);
                paramCreator.put(id, creator);
            }
            return creator;
        }
    }

    public String getFileName(String pkg, String name) throws IOException {
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

    public void createSourceFile(String pkg, String name, CharSequence sb) throws IOException {
        String file = pkg + '.' + name;
        JavaFileObject f = filer.createSourceFile(file);
        try (OutputStream os = f.openOutputStream()) {
            os.write(sb.toString().getBytes());
            os.flush();
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

    private static final String[] JAVA_LANG_TYPE = {"byte", "char", "short", "int", "long", "float", "double"};

    public boolean needImport(String clz) {
        int e = clz.lastIndexOf('.');
        if (e == -1) {
            return TextUtils.notIn(clz, JAVA_LANG_TYPE);
        }
        String srcPkg = clz.substring(0, e);
        if (srcPkg.equals("java.lang")) {
            return false;
        }
        return !srcPkg.equals(pkg);
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

    public String addServerName(String httpMethod, SourceServerName serverName) {
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
        injects.put(inject.type, inject);
    }

    public SourceInjectProvider getInject(String type) {
        return injects.get(type);
    }
}

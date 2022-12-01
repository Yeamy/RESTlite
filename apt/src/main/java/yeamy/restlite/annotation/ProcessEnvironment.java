package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.util.HashMap;

class ProcessEnvironment {
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private final HashMap<String, SourceParamCreator> paramCreator = new HashMap<>();
    private final String charset;
    private final SupportPatch supportPatch;
    private String pkg, response;
    private final TypeMirror closeable, httpResponse, inputStream, file;

    public ProcessEnvironment(ProcessingEnvironment env, TypeElement init) {
        messager = env.getMessager();
        typeUtils = env.getTypeUtils();
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
        PackageElement element = (PackageElement) init.getEnclosingElement();
        pkg = element.getQualifiedName().toString();
        Initialization ann = init.getAnnotation(Initialization.class);
        charset = ann.charset();
        supportPatch = ann.supportPatch();
        response = ann.response();
        //
        closeable = elementUtils.getTypeElement("java.io.Closeable").asType();
        httpResponse = elementUtils.getTypeElement("yeamy.restlite.HttpResponse").asType();
        inputStream = elementUtils.getTypeElement("java.io.InputStream").asType();
        file = elementUtils.getTypeElement("java.io.File").asType();
    }

    public Types getTypeUtils() {
        return typeUtils;
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

    public SourceParamCreator getBodyCreator(SourceServlet servlet, TypeMirror t, Body body) {
        return getParamCreator('b', servlet, t, body.creator(), body.tag());
    }

    public SourceParamCreator getExtraCreator(SourceServlet servlet, TypeMirror t, Extra extra) {
        return getParamCreator('e', servlet, t, extra.creator(), extra.tag());
    }

    private SourceParamCreator getParamCreator(char kind, SourceServlet servlet, TypeMirror t, String className, String tag) {
        TypeElement te = getTypeElement(t.toString());
        String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
        boolean samePackage = fpk.equals(servlet.getPackage());
        if (className.length() == 0) {
            Creator a = te.getAnnotation(Creator.class);
            if (a == null) {
                String id = kind + "c:" + te.getQualifiedName() + ":" + tag;
                SourceParamCreator arg = paramCreator.get(id);
                if (arg == null) {
                    arg = kind == 'b' ? SourceParamConstructor.body(this, samePackage, te, tag)
                            : SourceParamConstructor.extra(this, samePackage, te, tag);
                    arg.setID(id);
                    paramCreator.put(id, arg);
                }
                return arg;
            }
            className = a.className().length() > 0 ? a.className() : t.toString();
        }
        if (tag.length() > 0) {// by tag
            String id = kind + "t:" + className + ":" + tag;
            SourceParamCreator creator = paramCreator.get(id);
            if (creator == null) {
                creator = kind == 'b' ? SourceParamFactory.body(this, samePackage, className, t, tag)
                        : SourceParamFactory.extra(this, samePackage, className, t, tag);
                creator.setID(id);
                paramCreator.put(id, creator);
            }
            return creator;
        } else {// by type
            String id = kind + "f:" + className + ":" + t + ":" + (samePackage ? "" : servlet.getPackage());
            SourceParamCreator creator = paramCreator.get(id);
            if (creator == null) {
                creator = kind == 'b' ? SourceParamFactory.body(this, samePackage, className, t)
                        : SourceParamFactory.extra(this, samePackage, className, t);
                creator.setID(id);
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

    public SupportPatch supportPatch() {
        return supportPatch;
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
}

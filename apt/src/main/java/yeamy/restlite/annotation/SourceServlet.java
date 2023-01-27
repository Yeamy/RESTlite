package yeamy.restlite.annotation;

import jakarta.servlet.annotation.WebInitParam;

import javax.lang.model.element.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

class SourceServlet extends SourceClass {
    final ProcessEnvironment env;
    private final TypeElement element;
    private final Resource resource;
    private final HashMap<String, SourceMethodHttpMethod> httpMethods = new HashMap<>();
    private final SourceMethodOnError error;
    private final StringBuilder b = new StringBuilder();
    private final ArrayList<SourceInject> injects = new ArrayList<>();
    private boolean asyncSupported = false;

    public SourceServlet(ProcessEnvironment env, TypeElement element) {
        this.env = env;
        this.element = element;
        this.resource = element.getAnnotation(Resource.class);
        if (getResource().contains("/")) {
            throw new RuntimeException("Cannot create servlet with illegal resource in class:" + element.asType());
        }
        this.pkg = ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
        SourceMethodOnError error = null;
        for (Element li : element.getEnclosedElements()) {
            ElementKind kind = li.getKind();
            if (kind == ElementKind.FIELD) {
                Inject inject = li.getAnnotation(Inject.class);
                if (inject != null) {
                    injects.add(new SourceInject(this, (VariableElement) li));
                }
            } else if (kind == ElementKind.METHOD) {
                ExecutableElement eli = (ExecutableElement) li;
                GET get = eli.getAnnotation(GET.class);
                if (get != null) addMethodComponent("GET", eli, get.async(), get.asyncTimeout());
                POST post = eli.getAnnotation(POST.class);
                if (post != null) addMethodComponent("POST", eli, post.async(), post.asyncTimeout());
                PUT put = eli.getAnnotation(PUT.class);
                if (put != null) addMethodComponent("PUT", eli, put.async(), put.asyncTimeout());
                PATCH patch = eli.getAnnotation(PATCH.class);
                if (patch != null) addMethodComponent("PATCH", eli, patch.async(), patch.asyncTimeout());
                DELETE delete = eli.getAnnotation(DELETE.class);
                if (delete != null) addMethodComponent("DELETE", eli, delete.async(), delete.asyncTimeout());
                ERROR ann = eli.getAnnotation(ERROR.class);
                if (ann != null && error == null) {
                    error = new SourceMethodOnError(env, this, eli);
                }
            }
        }
        this.error = error;
    }

    public CharSequence getImplClass() {
        return element.getQualifiedName();
    }

    public String getResource() {
        return resource.value();
    }

    private void addMethodComponent(String ann, ExecutableElement element, boolean async, long asyncTimeout) {
        if (async) asyncSupported = true;
        SourceHttpMethodComponent method = new SourceHttpMethodComponent(env, this, element, async, asyncTimeout);
        SourceMethodHttpMethod httpMethod = httpMethods.get(ann);
        if (httpMethod == null) {
            httpMethod = new SourceMethodHttpMethod(env, this, ann);
            httpMethods.put(ann, httpMethod);
        }
        httpMethod.addMethod(method);
    }

    @Override
    public void create() throws IOException {
        imports("java.io.IOException");
        imports("jakarta.servlet.annotation.MultipartConfig");
        imports("jakarta.servlet.annotation.WebServlet");
        imports("jakarta.servlet.http.HttpServletResponse");
        imports("jakarta.servlet.ServletException");
        imports("yeamy.restlite.RESTfulRequest");
        imports("yeamy.restlite.RESTfulServlet");
        String impl = element.getSimpleName().toString();
        String name = env.getFileName(pkg, impl + "Servlet");
        createBody(impl, name);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(';');
        for (String p : new TreeSet<>(imports.values())) {
            if (env.needImport(p)) {
                sb.append("import ").append(p).append(';');
            }
        }
        sb.append(this.b);
        env.createSourceFile(pkg, name, sb);
    }

    private void createBody(String impl, String name) {
        {// WebServlet
            b.append("@WebServlet(");
            if (asyncSupported) {
                b.append("asyncSupported = true, value = \"");
            } else {
                b.append('"');
            }
            b.append('/').append(getResource()).append('"');
            WebInitParam[] initParams = resource.initParams();
            if (initParams.length > 0) {
                b.append(", initParams = {");
                for (WebInitParam p : initParams) {
                    b.append("@WebInitParam(name=\"").append(convStr(p.name()))
                            .append("\",value=\"").append(convStr(p.value())).append("\"),");
                }
                b.append('}');
            }
            b.append(')');
        }
        // MultipartConfig
        b.append("@MultipartConfig ");
        StringBuilder b2 = new StringBuilder();
        long maxFileSize = resource.maxFileSize();
        if (maxFileSize != -1) {
            b2.append(",maxFileSize = ").append(maxFileSize).append('L');
        }
        long maxRequestSize = resource.maxRequestSize();
        if (maxRequestSize != -1) {
            b2.append(",maxRequestSize = ").append(maxRequestSize).append('L');
        }
        String location = resource.tempLocation();
        if (location.length() > 0) {
            b2.append(",location = \"").append(convStr(location)).append('"');
        }
        int fileSizeThreshold = resource.fileSizeThreshold();
        if (fileSizeThreshold != 0) {
            b2.append(",fileSizeThreshold=").append(fileSizeThreshold);
        }
        if (b2.length() > 0) {
            b.append('(').append(b2, 1, b2.length()).append(") ");
        }
        // class
        b.append("public class ").append(name).append(" extends RESTfulServlet {");
        // serialVersionUID
        b.append("private static final long serialVersionUID = ").append(-1).append("L;");
        // impl
        b.append("private ").append(impl).append(" _impl = new ").append(impl).append("();");
        // field
        if (injects.size() > 0) {
            b.append("@Override public void init(")
                    .append(imports("jakarta.servlet.ServletConfig"))
                    .append(" config) throws ServletException { super.init(config);");
            ArrayList<SourceInject> closeable = new ArrayList<>();
            for (SourceInject inject : injects) {
                inject.createField(b);
                if (inject.needClose()) {
                    closeable.add(inject);
                }
            }
            b.append('}');
            if (closeable.size() > 0) {
                b.append("@Override public void destroy() { super.destroy();");
                for (SourceInject inject : closeable) {
                    inject.createClose(b);
                }
                b.append('}');
            }
        }
        // method
        boolean catchException = error != null;
        for (SourceMethodHttpMethod method : httpMethods.values()) {
            method.create(catchException);
        }
        // error
        if (catchException) {
            error.create();
        }
        b.append("}");
    }

    public int length() {
        return b.length();
    }

    public SourceServlet append(CharSequence s) {
        b.append(s);
        return this;
    }

    public SourceServlet append(CharSequence str, int start, int end) {
        b.append(str, start, end);
        return this;
    }

    public SourceServlet append(char c) {
        b.append(c);
        return this;
    }

    public SourceServlet append(long l) {
        b.append(l);
        return this;
    }

    public SourceServlet deleteLast(int i) {
        int l = b.length();
        b.delete(l - i, l);
        return this;
    }

    public boolean containsError() {
        return error != null;
    }
}

package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

class SourceServlet extends SourceClass {
    private static final Class<?>[] METHODS = {GET.class, POST.class, PUT.class, PATCH.class, DELETE.class};
    private final TypeElement element;
    private final Resource resource;
    private final HashMap<Class<?>, SourceMethodHttpMethod> httpMethods = new HashMap<>();
    private final SourceMethodOnError error;
    private final StringBuilder b = new StringBuilder();
    private final ArrayList<SourceInject> injects = new ArrayList<>();

    public SourceServlet(ProcessEnvironment env, TypeElement element) {
        super(env);
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
                addMethod(eli);
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

    @SuppressWarnings("unchecked")
    private void addMethod(ExecutableElement element) {
        ArrayList<Class<?>> methods = new ArrayList<>(METHODS.length);
        for (Class<?> clz : METHODS) {
            if (element.getAnnotation((Class<? extends Annotation>) clz) != null) {
                methods.add(clz);
            }
        }
        if (methods.size() == 0) {
            return;
        }
        SourceHttpMethodComponent method = new SourceHttpMethodComponent(env, this, element);
        for (Class<?> clz : methods) {
            SourceMethodHttpMethod httpMethod = httpMethods.get(clz);
            if (httpMethod == null) {
                httpMethod = new SourceMethodHttpMethod(env, this, clz.getSimpleName());
                httpMethods.put(clz, httpMethod);
            }
            httpMethod.addMethod(method);
        }
    }

    @Override
    public void create() throws IOException {
        imports("jakarta.servlet.annotation.MultipartConfig");
        imports("jakarta.servlet.annotation.WebServlet");
        imports("yeamy.restlite.RESTfulServlet");
        imports("jakarta.servlet.http.HttpServletResponse");
        String impl = element.getSimpleName().toString();
        String name = env.getFileName(pkg, impl + "Servlet");
        createBody(impl, name);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";");
        for (String p : new TreeSet<>(imports.values())) {
            if (env.needImport(p)) {
                sb.append("import ").append(p).append(";");
            }
        }
        sb.append(this.b);
        env.createSourceFile(pkg, name, sb);
    }

    private void createBody(String impl, String name) {
        {// WebServlet
            b.append("@WebServlet(");
            if (resource.asyncSupported()) {
                b.append("asyncSupported = true, value = \"");
            } else {
                b.append('"');
            }
            b.append('/').append(getResource()).append("\")");
        }
        // MultipartConfig
        b.append("@MultipartConfig ");
        StringBuilder b2 = new StringBuilder("(");
        long maxFileSize = resource.maxFileSize();
        if (maxFileSize > -1) {
            b2.append("maxFileSize = ").append(maxFileSize).append('L');
        }
        long maxRequestSize = resource.maxRequestSize();
        if (maxRequestSize > -1) {
            if (b2.length() > 0) {
                b.append(", ");
            }
            b.append("maxRequestSize = ").append(maxRequestSize).append('L');
        }
        b2.append(')');
        if (b2.length() > 3) {
            b.append(b2);
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
            for (SourceInject inject : injects) {
                inject.create(b);
            }
            b.append('}');
        }
        // method
        for (SourceMethodHttpMethod method : httpMethods.values()) {
            method.create(error != null);
        }
        // error
        if (error != null) {
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

    public void deleteLast(int i) {
        int l = b.length();
        b.delete(l - i, l);
    }
}

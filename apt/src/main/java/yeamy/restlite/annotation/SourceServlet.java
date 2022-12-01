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
    private final HashMap<Class<?>, SourceServletHttpMethod> httpMethods = new HashMap<>();
    private final SourceServletOnError error;
    private final StringBuilder b = new StringBuilder();

    public SourceServlet(ProcessEnvironment env, TypeElement element) {
        super(env);
        this.element = element;
        this.pkg = ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
        this.error = new SourceServletOnError(env, this);
        for (Element li : element.getEnclosedElements()) {
            if (li.getKind() == ElementKind.METHOD) {
                addMethod((ExecutableElement) li);
                if (element.getAnnotation(ERROR.class) != null) {
                    error.addMethod(new SourceDispatchOnError(env, this, (ExecutableElement) li));
                }
            }
        }
    }

    public String getImplName() {
        return element.getSimpleName().toString();
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
        SourceDispatchService method = new SourceDispatchService(env, this, element);
        for (Class<?> clz : methods) {
            SourceServletHttpMethod httpMethod = httpMethods.get(clz);
            if (httpMethod == null) {
                httpMethod = new SourceServletHttpMethod(env, this, clz.getSimpleName());
                httpMethods.put(clz, httpMethod);
            }
            httpMethod.addMethod(method);
        }
    }

    @Override
    public void create() throws IOException {
        imports("javax.servlet.annotation.MultipartConfig");
        imports("javax.servlet.annotation.WebServlet");
        imports("yeamy.restlite.RESTfulServlet");
        String impl = getImplName();
        String name = env.getFileName(pkg, impl + "Servlet");
        createBody(impl, name);

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";");
        for (String p : new TreeSet<>(imports.values())) {
            if (!p.substring(0, p.lastIndexOf('.')).equals(pkg)) {
                sb.append("import ").append(p).append(";");
            }
        }
        sb.append(this.b);
        env.createSourceFile(pkg, name, sb);
    }

    private void createBody(String impl, String name) {
        Resource r = element.getAnnotation(Resource.class);
        {// WebServlet
            b.append("@WebServlet(");
            if (r.asyncSupported()) {
                b.append("asyncSupported = true, value = \"");
            } else {
                b.append('"');
            }
            b.append('/').append(r.value()).append("\")\n");
        }
        // MultipartConfig
        b.append("@MultipartConfig");
        StringBuilder b2 = new StringBuilder("(");
        long maxFileSize = r.maxFileSize();
        if (maxFileSize > -1) {
            b2.append("maxFileSize = ").append(maxFileSize).append('L');
        }
        long maxRequestSize = r.maxRequestSize();
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
        b.append('\n');
        // class
        b.append("public class ").append(name).append(" extends RESTfulServlet {");
        // serialVersionUID
        b.append("private static final long serialVersionUID = ").append(-1).append("L;");
        // impl
        b.append("private ").append(impl).append(" impl = new ").append(impl).append("();");
        // method
        for (SourceServletHttpMethod method : httpMethods.values()) {
            try {
                method.create();
            } catch (Exception e) {
                b.append("/**").append(e).append("*/");
                break;
            }
        }
        // error
        try {
            error.create();
        } catch (Exception e) {
            b.append("/**").append(e).append("*/");
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

package yeamy.restlite.annotation;

import jakarta.servlet.annotation.WebInitParam;

import javax.lang.model.element.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static yeamy.restlite.annotation.SupportType.T_HttpServletResponse;
import static yeamy.restlite.annotation.SupportType.T_RESTfulRequest;

class SourceServlet extends SourceClass {
    final ProcessEnvironment env;
    private final String impl, name;
    private final TypeElement element;
    private final RESTfulResource resource;
    private final HashMap<String, SourceServletMethod> httpMethods = new HashMap<>();
    private final SourceMethodOnError error;
    private final StringBuilder b = new StringBuilder();
    private final ArrayList<SourceInject> injectFields = new ArrayList<>();
    private boolean asyncSupported = false;

    public SourceServlet(ProcessEnvironment env, TypeElement element) {
        super(((PackageElement) element.getEnclosingElement()).getQualifiedName().toString());
        this.env = env;
        this.element = element;
        this.resource = element.getAnnotation(RESTfulResource.class);
        this.impl = element.getSimpleName().toString();
        this.name = env.getFileName(pkg, impl + "Servlet");
        if (getRESTfulResource().contains("/")) {
            throw new RuntimeException("Cannot create servlet with illegal REST resource in class:" + element.asType());
        }
        SourceMethodOnError error = null;
        for (Element li : element.getEnclosedElements()) {
            ElementKind kind = li.getKind();
            if (kind == ElementKind.FIELD) {
                Inject inject = li.getAnnotation(Inject.class);
                if (inject != null) {
                    injectFields.add(SourceVariableHelper.getInject(env, this, (VariableElement) li, inject));
                }
            } else if (kind == ElementKind.METHOD) {
                ExecutableElement eli = (ExecutableElement) li;
                GET get = eli.getAnnotation(GET.class);
                if (get != null) addMethodComponent("GET", eli, get.async(), get.asyncTimeout(), get.permission());
                POST post = eli.getAnnotation(POST.class);
                if (post != null) addMethodComponent("POST", eli, post.async(), post.asyncTimeout(), post.permission());
                PUT put = eli.getAnnotation(PUT.class);
                if (put != null) addMethodComponent("PUT", eli, put.async(), put.asyncTimeout(), put.permission());
                PATCH patch = eli.getAnnotation(PATCH.class);
                if (patch != null) addMethodComponent("PATCH", eli, patch.async(), patch.asyncTimeout(), patch.permission());
                DELETE delete = eli.getAnnotation(DELETE.class);
                if (delete != null) addMethodComponent("DELETE", eli, delete.async(), delete.asyncTimeout(), delete.permission());
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

    public String getRESTfulResource() {
        return resource.value();
    }

    private void addMethodComponent(String ann, ExecutableElement element, boolean async, long asyncTimeout, String permission) {
        if (async) asyncSupported = true;
        SourceImplMethodDispatcher method = new SourceImplMethodDispatcher(env, this, element, async, asyncTimeout, permission);
        SourceServletMethod httpMethod = httpMethods.get(ann);
        if (httpMethod == null) {
            httpMethod = new SourceServletMethod(env, this, ann);
            httpMethods.put(ann, httpMethod);
        }
        httpMethod.addComponent(method);
    }

    @Override
    public void create() throws IOException {
        imports("java.io.IOException");
        imports("jakarta.servlet.annotation.MultipartConfig");
        imports("jakarta.servlet.annotation.WebServlet");
        imports(T_HttpServletResponse);
        imports("jakarta.servlet.ServletException");
        imports(T_RESTfulRequest);
        imports("yeamy.restlite.RESTfulServlet");
        createBody();
        createSourceFile(env.processingEnv, name, b);
    }

    private void createBody() {
        {// WebServlet
            b.append("@WebServlet(");
            if (asyncSupported) {
                b.append("asyncSupported = true, value = \"");
            } else {
                b.append('"');
            }
            b.append('/').append(getRESTfulResource()).append('"');
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
        if (!location.isEmpty()) {
            b2.append(",location = \"").append(convStr(location)).append('"');
        }
        int fileSizeThreshold = resource.fileSizeThreshold();
        if (fileSizeThreshold != 0) {
            b2.append(",fileSizeThreshold=").append(fileSizeThreshold);
        }
        if (!b2.isEmpty()) {
            b.append('(').append(b2, 1, b2.length()).append(") ");
        }
        // class
        b.append("public class ").append(name).append(" extends RESTfulServlet {");
        // serialVersionUID
        b.append("private static final long serialVersionUID = ").append(-1).append("L;");
        // impl
        b.append("private ").append(impl).append(" _impl = new ").append(impl).append("();");
        // field
        if (injectFields.size() > 0) {
            b.append("@Override public void init(")
                    .append(imports("jakarta.servlet.ServletConfig"))
                    .append(" config) throws ServletException { super.init(config);");
            ArrayList<SourceInject> closeable = new ArrayList<>();
            for (SourceInject inject : injectFields) {
                inject.writeField(b, this);
                if (inject.isCloseable()) {
                    closeable.add(inject);
                }
            }
            b.append('}');
            if (closeable.size() > 0) {
                b.append("@Override public void destroy() { super.destroy();");
                for (SourceInject inject : closeable) {
                    inject.writeCloseField(b, this);
                }
                b.append('}');
            }
        }
        // method
        boolean hasOnError = error != null;
        for (SourceServletMethod method : httpMethods.values()) {
            method.create(hasOnError);
        }
        // error
        if (hasOnError) {
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

    public boolean isSamePackage(TypeElement te) {
        String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
        return fpk.equals(pkg);
    }

    public String getImpl() {
        return impl;
    }
}

package yeamy.restlite.annotation;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import yeamy.utils.TextUtils;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

class SourceMain extends SourceClass {
    private final ProcessingEnvironment env;
    private final TomcatConfig conf;
    private final Set<? extends Element> methods;
    private final Set<PositionBean> listeners = new TreeSet<>();
    private final Set<PositionBean> filters = new TreeSet<>();
    private final Set<Element> servlets = new HashSet<>();
    private final String name;

    public SourceMain(ProcessingEnvironment env, Element element, Set<? extends Element> methods,
                      TomcatConfig conf) {
        this.env = env;
        String main = conf.main();
        if (main.length() == 0) {
            this.pkg = ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
            this.name = "Main";
        } else {
            int b = main.lastIndexOf('.');
            if (b == -1) b = 0;
            this.pkg = main.substring(0, b);
            this.name = main.substring(b);
        }
        this.methods = methods;
        this.conf = conf;
        imports("org.apache.catalina.Context");
        imports("org.apache.catalina.LifecycleException");
        imports("org.apache.catalina.Wrapper");
        imports("org.apache.catalina.startup.Tomcat");
        imports("org.apache.tomcat.util.descriptor.web.FilterDef");
        imports("org.apache.tomcat.util.descriptor.web.FilterMap");
        imports("java.util.Properties");
    }

    void add(Set<? extends Element> listeners,
             Set<? extends Element> filters,
             Set<? extends Element> servlets) {
        for (Element element : listeners) {
            this.listeners.add(new PositionBean(element));
        }
        for (Element element : filters) {
            this.filters.add(new PositionBean(element));
        }
        this.servlets.addAll(servlets);
    }

    @Override
    public void create() throws IOException {
        String file = pkg + '.' + name;
        StringBuilder sb = new StringBuilder();
        if (pkg.length() > 0) {
            sb.append("package ").append(pkg).append(';');
        }
        sb.append("import static yeamy.restlite.tomcat.TomcatUtils.*;");
        StringBuilder content = new StringBuilder();
        createContent(content);
        for (String clz : imports.values()) {
            sb.append("import ").append(clz).append(';');
        }
        sb.append("public class ").append(name).append('{').append(content).append('}');
        JavaFileObject f = env.getFiler().createSourceFile(file);
        try (OutputStream os = f.openOutputStream()) {
            os.write(sb.toString().getBytes());
            os.flush();
        }
    }

    private void createContent(StringBuilder sb) {
        sb.append("private static final Tomcat tomcat=new Tomcat();");
        // load listener, filter, servlet
        sb.append("private static void load(Context context) {");
        if (listeners.size() > 0) {
            sb.append("context.setApplicationEventListeners(new Object[]{");
            for (PositionBean bean : listeners) {
                TypeElement element = bean.element;
                sb.append("new ").append(imports(element)).append("(),");
            }
            sb.deleteCharAt(sb.length() - 1).append("});");
        }
        if (filters.size() > 0) {
            sb.append("FilterDef fd;FilterMap fm;");
            for (PositionBean bean : filters) {
                TypeElement element = bean.element;
                WebFilter ann = element.getAnnotation(WebFilter.class);
                Set<String> urlPatterns = new HashSet<>();
                Collections.addAll(urlPatterns, ann.value());
                Collections.addAll(urlPatterns, ann.urlPatterns());
                if (urlPatterns.size() == 0) continue;
                String clz = imports(element);
                String name = ann.filterName();
                if (name.length() == 0) name = clz.contains(".") ? clz.replace(".", "_") : clz;
                sb.append("/* ").append(clz).append(" */");
                // fd
                sb.append("fd = new FilterDef();fd.setFilterName(\"").append(name).append("\");");
                for (WebInitParam param : ann.initParams()) {
                    sb.append("fd.addInitParameter(\"").append(param.name())
                            .append("\",\"").append(param.value()).append("\");");
                }
                sb.append("fd.setAsyncSupported(\"").append(ann.asyncSupported()).append("\");");
                sb.append("fd.setFilter(new ").append(clz).append("());");
                // fm
                sb.append("fm = new FilterMap();fm.setFilterName(\"").append(name).append("\");");
                for (String uri : urlPatterns) {
                    sb.append("fm.addURLPattern(\"").append(uri).append("\");");
                }
                sb.append("context.addFilterDef(fd);context.addFilterMap(fm);");
            }
        }
        Messager msg = env.getMessager();
        if (servlets.size() > 0) {
            sb.append("Wrapper wrapper;");
            for (Element element : servlets) {
                WebServlet ann = element.getAnnotation(WebServlet.class);
                Set<String> urlPatterns = new HashSet<>();
                Collections.addAll(urlPatterns, ann.value());
                Collections.addAll(urlPatterns, ann.urlPatterns());
                for (String url : urlPatterns) {
                    if (url.indexOf('/', 1) > 0) {
                        msg.printMessage(Diagnostic.Kind.WARNING, "Custom url may cause error: " + url);
                    }
                }
                if (urlPatterns.size() == 0) continue;
                String clz = imports((TypeElement) element);
                String name = ann.name();
                if (name.length() == 0) name = clz.replace(".", "_");
                sb.append("wrapper = tomcat.addServlet(\"\", \"").append(name).append("\", new ").append(clz).append("());");
                for (String uri : urlPatterns) {
                    sb.append("wrapper.addMapping(\"").append(uri).append("\");");
                }
                MultipartConfig multipart = element.getAnnotation(MultipartConfig.class);
                if (multipart != null) {
                    sb.append("wrapper.setMultipartConfigElement(new ")
                            .append(imports("jakarta.servlet.MultipartConfigElement"))
                            .append("(").append(clz).append(".class.getAnnotation(")
                            .append(imports("jakarta.servlet.annotation.MultipartConfig"))
                            .append(".class)));");
                }
            }
        }
        sb.append('}');
        // createProperties
        sb.append("private static Properties createProperties(){Properties properties=new Properties();");
        if (TextUtils.isNotEmpty(conf.hostName())) {
            setProperty(sb, "hostName", conf.hostName());
        }
        if (TextUtils.isNotEmpty(conf.baseDir())) {
            setProperty(sb, "baseDir", conf.baseDir());
        }
        if (conf.maxThreads() > 0) {
            setProperty(sb, "baseDir", conf.maxThreads());
        }
        if (conf.minSpareThreads() > 0) {
            setProperty(sb, "minSpareThreads", conf.minSpareThreads());
        }
        if (conf.connectionTimeout() > 0) {
            setProperty(sb, "connectionTimeout", conf.connectionTimeout());
        }
        if (conf.maxConnections() > 0) {
            setProperty(sb, "maxConnections", conf.maxConnections());
        }
        if (conf.acceptCount() > 0) {
            setProperty(sb, "acceptCount", conf.acceptCount());
        }
        int i = 0;
        for (Connector connector : conf.connector()) {
            setProperty(sb, "connector" + i + ".port", connector.port());
            if (TextUtils.isNotEmpty(connector.hostName())) {
                setProperty(sb, "connector" + i + ".hostName", connector.hostName());
            }
            if (connector.redirectPort() > 0) {
                setProperty(sb, "connector" + i + ".redirectPort", connector.redirectPort());
            }
            setProperty(sb, "connector" + i + ".secure", connector.secure());
            if (connector.secure()) {
                setProperty(sb, "connector" + i + ".sslProtocol", connector.sslProtocol());
                setProperty(sb, "connector" + i + ".ciphers", connector.ciphers());
                setProperty(sb, "connector" + i + ".keyStoreType", connector.keyStoreType());
                setProperty(sb, "connector" + i + ".keyStoreFile", convStr(connector.keyStoreFile()));
                setProperty(sb, "connector" + i + ".keyStorePass", convStr(connector.keyStorePass()));
            }
            i++;
        }
        sb.append("return properties;}");
        // run before tomcat
        if (methods.size() > 0) {
            sb.append("private static void runBeforeTomcat(){");
            for (Element e : methods) {
                Set<Modifier> modifiers = e.getModifiers();
                if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
                    msg.printMessage(Diagnostic.Kind.WARNING, "Methods run before Tomcat must be public static: "
                            + e.asType().toString() + "." + e.getSimpleName());
                } else if (((ExecutableElement) e).getParameters().size() > 0) {
                    msg.printMessage(Diagnostic.Kind.WARNING, "Methods run before Tomcat must have no param: "
                            + e.asType().toString() + "." + e.getSimpleName());
                } else {
                    sb.append(imports(e.asType().toString())).append(".")
                            .append(e.getSimpleName()).append("();");
                }
            }
            sb.append('}');
        }
        // main
        sb.append("public static void main(String[] args) {Runtime.getRuntime().addShutdownHook(new Thread(() -> {" +
                "try {tomcat.stop();tomcat.destroy();} catch (LifecycleException e) {e.printStackTrace();}}));" +
                "try {runBeforeTomcat();Properties properties = getProperties(args);" +
                "if(properties==null){properties=createProperties();}Context context = addContext(properties, tomcat);" +
                "addConnectors(properties, tomcat);load(context);tomcat.start();tomcat.getServer().await();}" +
                "catch (Exception e) {e.printStackTrace();}}");
    }

    private static void setProperty(StringBuilder b, String key, Object value) {
        b.append("properties.setProperty(\"").append(key).append("\", \"").append(value).append("\");");
    }

    private static class PositionBean implements Comparable<PositionBean> {
        int position;
        TypeElement element;

        public PositionBean(Element element) {
            this.element = (TypeElement) element;
            Position p = element.getAnnotation(Position.class);
            this.position = p == null ? Integer.MAX_VALUE : p.value();
        }

        @Override
        public int compareTo(PositionBean o) {
            return position = o.position;
        }
    }

}

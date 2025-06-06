package yeamy.restlite.annotation;

import jakarta.annotation.Priority;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

class SourceMain extends SourceClass {
    private final ProcessingEnvironment env;
    private final TomcatConfig conf;
    private final Set<? extends Element> methodsBeforeTomcat;
    private final Set<PriorityBean> listeners = new TreeSet<>();
    private final Set<PriorityBean> filters = new TreeSet<>();
    private final Set<Element> servlets = new HashSet<>();
    private final String name;

    public SourceMain(ProcessingEnvironment env, TomcatConfig conf, String pkg, String name,
                      Set<? extends Element> methodsBeforeTomcat) {
        super(pkg);
        this.env = env;
        this.name = name;
        this.methodsBeforeTomcat = methodsBeforeTomcat;
        this.conf = conf;
        imports("org.apache.catalina.Context");
        imports("org.apache.catalina.LifecycleException");
        imports("org.apache.catalina.Wrapper");
        imports("org.apache.catalina.startup.Tomcat");
        imports("org.apache.tomcat.util.descriptor.web.FilterDef");
        imports("org.apache.tomcat.util.descriptor.web.FilterMap");
        imports("java.util.Collections");
        imports("java.util.Properties");
    }

    void add(Set<? extends Element> listeners,
             Set<? extends Element> filters,
             Set<? extends Element> servlets) {
        for (Element element : listeners) {
            this.listeners.add(new PriorityBean(element));
        }
        for (Element element : filters) {
            this.filters.add(new PriorityBean(element));
        }
        this.servlets.addAll(servlets);
    }

    @Override
    public void create() throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("import static yeamy.restlite.tomcat.TomcatUtils.*;");
        content.append("public class ").append(name).append('{');
        createContent(content);
        content.append('}');
        String qualifiedName = pkg + '.' + name;
        createSourceFile(env, qualifiedName, content);
        createManifest(env, qualifiedName);
    }

    public static void createManifest(ProcessingEnvironment env, String mainClass) throws IOException {
        FileObject f = env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/MANIFEST.MF");
        try (OutputStream os = f.openOutputStream()) {
            os.write("Manifest-Version: 1.0\nMain-Class: ".getBytes());
            os.write(mainClass.getBytes());
            os.write("\n".getBytes());
//            Implementation-Version: 1.0.0
            os.flush();
        }
    }

    private void createContent(StringBuilder sb) {
        sb.append("""
                private static final Tomcat tomcat = new Tomcat();
                
                private static void load(Context context) {
                    context.setSessionTimeout(0);
                """);
        // load listener, filter, servlet
        if (!listeners.isEmpty()) {
            sb.append("context.setApplicationEventListeners(new Object[]{");
            for (PriorityBean bean : listeners) {
                TypeElement element = bean.element;
                sb.append("new ").append(imports(element)).append("(),");
            }
            sb.deleteCharAt(sb.length() - 1).append("});");
        }
        if (!filters.isEmpty()) {
            sb.append("FilterDef fd;FilterMap fm;");
            for (PriorityBean bean : filters) {
                TypeElement element = bean.element;
                WebFilter ann = element.getAnnotation(WebFilter.class);
                Set<String> urlPatterns = new HashSet<>();
                Collections.addAll(urlPatterns, ann.value());
                Collections.addAll(urlPatterns, ann.urlPatterns());
                if (urlPatterns.isEmpty()) continue;
                String clz = imports(element);
                String name = ann.filterName();
                if (name.isEmpty()) name = clz.contains(".") ? clz.replace(".", "_") : clz;
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
        if (!servlets.isEmpty()) {
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
                if (urlPatterns.isEmpty()) continue;
                String clz = imports((TypeElement) element);
                String name = ann.name();
                if (name.isEmpty()) name = clz.replace(".", "_");
                sb.append("wrapper = tomcat.addServlet(\"\", \"").append(name).append("\", new ").append(clz).append("());");
                for (String uri : urlPatterns) {
                    sb.append("wrapper.addMapping(\"").append(uri).append("\");");
                }
                MultipartConfig multipart = element.getAnnotation(MultipartConfig.class);
                if (multipart != null) {
                    sb.append("wrapper.setMultipartConfigElement(new ")
                            .append(imports("jakarta.servlet.MultipartConfigElement"))
                            .append('(').append(clz).append(".class.getAnnotation(")
                            .append(imports("jakarta.servlet.annotation.MultipartConfig"))
                            .append(".class)));");
                }
            }
        }
        sb.append('}');
        // createProperties
        sb.append("private static Properties createProperties(){Properties properties=new Properties();");
        if (!conf.baseDir().isEmpty()) {
            setProperty(sb, "baseDir", conf.baseDir());
        }
        if (!conf.contextDir().isEmpty()) {
            setProperty(sb, "contextDir", conf.contextDir());
        }
        if (conf.maxThreads() > 0) {
            setProperty(sb, "maxThreads", conf.maxThreads());
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
            String hostName = connector.hostName();
            if ("".equals(hostName)) {
                hostName = "connector" + i;
            }
            setProperty(sb, "connector" + i + ".hostName", hostName);
            setProperty(sb, "connector" + i + ".port", connector.port());
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
        // main
        sb.append("""
                public static void main(String[] args) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    tomcat.stop();
                    tomcat.destroy();
                } catch (LifecycleException e) {e.printStackTrace();}}));
                try {
                """);
        // run before tomcat
        for (Element e : methodsBeforeTomcat) {
            Set<Modifier> modifiers = e.getModifiers();
            if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
                msg.printMessage(Diagnostic.Kind.WARNING, "Methods run before Tomcat must be public static: "
                        + e.asType().toString() + '.' + e.getSimpleName());
            } else {
                sb.append(imports(e.asType())).append('.').append(e.getSimpleName()).append('(');
                List<? extends VariableElement> params = ((ExecutableElement) e).getParameters();
                for (VariableElement p : params) {
                    switch (p.asType().toString()) {
                        case "java.lang.String[]" -> sb.append("args,");
                        case "org.apache.catalina.startup.Tomcat" -> sb.append("tomcat,");
                        case "org.apache.catalina.Context" -> sb.append("context,");
                        case "int", "long", "float", "double" -> sb.append("0,");
                        case "short" -> sb.append("(short)0,");
                        case "byte" -> sb.append("(byte)0,");
                        case "boolean" -> sb.append("false,");
                        default -> sb.append("null,");
                    }
                }
                if (params.isEmpty()) {
                    sb.append(')');
                } else {
                    sb.setCharAt(sb.length() - 1, ')');
                }
                sb.append(';');
            }
        }
        sb.append("""
                    Properties properties = getProperties(args);
                    if(properties==null){properties=createProperties();}
                    Context context = addContext(properties, tomcat);
                    addConnectors(properties, tomcat);
                    load(context);tomcat.start();
                    tomcat.getServer().await();
                }catch (Exception e) {e.printStackTrace();}
                }
                """);
    }

    private static void setProperty(StringBuilder b, String key, Object value) {
        b.append("properties.setProperty(\"").append(key).append("\", \"").append(value).append("\");");
    }

    private static class PriorityBean implements Comparable<PriorityBean> {
        int position;
        TypeElement element;

        public PriorityBean(Element element) {
            this.element = (TypeElement) element;
            Priority p = element.getAnnotation(Priority.class);
            this.position = p == null ? Integer.MAX_VALUE : p.value();
        }

        @Override
        public int compareTo(PriorityBean o) {
            return position = o.position;
        }
    }

}

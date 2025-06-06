package yeamy.restlite.annotation;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

public class MainAnnotationProcessor extends AbstractProcessor {

    private final Set<String> supportedAnnotationTypes = new HashSet<>();
    private SourceMain source;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        supportedAnnotationTypes.add(WebListener.class.getCanonicalName());
        supportedAnnotationTypes.add(WebFilter.class.getCanonicalName());
        supportedAnnotationTypes.add(WebServlet.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            try {
                source.create();
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
            return false;
        }
        if (source == null) {
            Set<? extends Element> conf = roundEnv.getElementsAnnotatedWith(Configuration.class);
            for (Element element : conf) {
                for (Element e : roundEnv.getElementsAnnotatedWith(TomcatConfig.class)) {
                    TomcatConfig config = e.getAnnotation(TomcatConfig.class);
                    String[] clz = getMainClassName(config, element);
                    source = new SourceMain(processingEnv, config, clz[0], clz[1],
                            roundEnv.getElementsAnnotatedWith(RunBeforeTomcat.class));
                    break;
                }
                break;
            }
        }
        if (source == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot create class Main because no annotation Configuration found!");
            return false;
        }
        source.add(roundEnv.getElementsAnnotatedWith(WebListener.class),
                roundEnv.getElementsAnnotatedWith(WebFilter.class),
                roundEnv.getElementsAnnotatedWith(WebServlet.class));
        return false;
    }

    public static String[] getMainClassName(TomcatConfig config, Element element) {
        String main = config.main();
        if (main.isEmpty()) {
            return new String[]{
                    ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString(),
                    "Main"};
        } else {
            int b = main.lastIndexOf('.');
            if (b == -1) b = 0;
            return new String[]{main.substring(0, b), main.substring(b)};
        }
    }
}
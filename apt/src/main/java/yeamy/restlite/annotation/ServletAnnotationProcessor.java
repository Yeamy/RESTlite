package yeamy.restlite.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ServletAnnotationProcessor extends AbstractProcessor {

    private final Set<String> supportedAnnotationTypes = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        supportedAnnotationTypes.add(Initialization.class.getCanonicalName());
        supportedAnnotationTypes.add(Resource.class.getCanonicalName());
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
        ProcessEnvironment env = null;
        for (Element element : roundEnv.getElementsAnnotatedWith(Initialization.class)) {
            TypeElement init = (TypeElement) element;
            env = new ProcessEnvironment(processingEnv, init);
            break;
        }
        if (env == null) {
            return false;
        }
        // filter
        try {
            new SourceWebFilter(env).create();
        } catch (Exception e) {
            env.error(e);
            return false;
        }
        // class
        ArrayList<SourceServlet> servlets = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Resource.class)) {
            SourceServlet servlet = new SourceServlet(env, (TypeElement) element);
            servlets.add(servlet);
        }
        // servlet
        for (SourceServlet servlet : servlets) {
            try {
                servlet.create();
            } catch (Exception e) {
                env.error(e);
                return false;
            }
        }
        // listener
        try {
            new SourceWebListener(env).create();
        } catch (Exception e) {
            env.error(e);
            return false;
        }
        return false;
    }

}
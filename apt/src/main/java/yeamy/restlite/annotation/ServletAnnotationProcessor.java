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

/**
 * Annotation processor to generate program code
 */
public class ServletAnnotationProcessor extends AbstractProcessor {

    private final Set<String> supportedAnnotationTypes = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        supportedAnnotationTypes.add(Configuration.class.getCanonicalName());
        supportedAnnotationTypes.add(RESTfulResource.class.getCanonicalName());
        supportedAnnotationTypes.add(HeaderProcessor.class.getCanonicalName());
        supportedAnnotationTypes.add(CookieProcessor.class.getCanonicalName());
        supportedAnnotationTypes.add(BodyProcessor.class.getCanonicalName());
        supportedAnnotationTypes.add(PartProcessor.class.getCanonicalName());
        supportedAnnotationTypes.add(ParamProcessor.class.getCanonicalName());
        supportedAnnotationTypes.add(InjectProvider.class.getCanonicalName());
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
        for (Element init : roundEnv.getElementsAnnotatedWith(Configuration.class)) {
            env = new ProcessEnvironment(processingEnv, init);
            break;
        }
        if (env == null) {
            return false;
        }
        // header processor
        for (Element element : roundEnv.getElementsAnnotatedWith(HeaderProcessor.class)) {
            env.addSourceHeaderProcessor(element, element.getAnnotation(HeaderProcessor.class));
        }
        // cookie processor
        for (Element element : roundEnv.getElementsAnnotatedWith(CookieProcessor.class)) {
            env.addSourceCookieProcessor(element, element.getAnnotation(CookieProcessor.class));
        }
        // body processor
        for (Element element : roundEnv.getElementsAnnotatedWith(BodyProcessor.class)) {
            env.addBodyProcessor(element, element.getAnnotation(BodyProcessor.class));
        }
        // part processor
        for (Element element : roundEnv.getElementsAnnotatedWith(PartProcessor.class)) {
            env.addPartProcessor(element, element.getAnnotation(PartProcessor.class));
        }
        // param processor
        for (Element element : roundEnv.getElementsAnnotatedWith(ParamProcessor.class)) {
            env.addParamProcessor(element, element.getAnnotation(ParamProcessor.class));
        }
        // inject provider
        for (Element element : roundEnv.getElementsAnnotatedWith(InjectProvider.class)) {
            env.addInjectProvider(element, element.getAnnotation(InjectProvider.class));
        }
        // embed
        TypeElement tomcatConfig = env.getTypeElement("yeamy.restlite.annotation.TomcatConfig");
        boolean embed = tomcatConfig != null && roundEnv.getElementsAnnotatedWith(tomcatConfig).size() > 0;
        // filter
        try {
            new SourceWebFilter(env, embed).create();
        } catch (Exception e) {
            env.error(e);
            return false;
        }
        // class
        ArrayList<SourceServlet> servlets = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(RESTfulResource.class)) {
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
            new SourceWebListener(env, embed).create();
        } catch (Exception e) {
            env.error(e);
            return false;
        }
        return false;
    }

}
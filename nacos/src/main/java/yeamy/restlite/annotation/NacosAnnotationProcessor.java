package yeamy.restlite.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

public class NacosAnnotationProcessor extends AbstractProcessor {

    private final Set<String> supportedAnnotationTypes = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        supportedAnnotationTypes.add(NacosRemoteServer.class.getCanonicalName());
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
        Element executor = getExecutor(roundEnv);
        for (Element element : roundEnv.getElementsAnnotatedWith(NacosRemoteServer.class)) {
            try {
                if (element.getKind().equals(ElementKind.INTERFACE)) {
                    new SourceService(processingEnv,
                            (TypeElement) element,
                            element.getAnnotation(NacosRemoteServer.class),
                            executor)
                            .create();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Element getExecutor(RoundEnvironment roundEnv) {
        for (Element executor : roundEnv.getElementsAnnotatedWith(NacosExecutor.class)) {
            Set<Modifier> ms = executor.getModifiers();
            if (ms.contains(Modifier.PUBLIC) && ms.contains(Modifier.STATIC)) {
                return executor;
            }
        }
        return null;
    }
}

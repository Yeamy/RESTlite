package yeamy.restlite.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Map;

record PartFactoryBean(PartFactory ann, String name) {

    public static PartFactoryBean get(VariableElement param) {
        PartFactory ann;
        for (AnnotationMirror am : param.getAnnotationMirrors()) {
            ann = am.getAnnotationType().asElement().getAnnotation(PartFactory.class);
            if (ann == null) continue;
            String method = ann.nameMethod();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(method)) {
                    return new PartFactoryBean(ann, entry.getValue().getValue().toString());
                }
            }
        }
        return null;
    }
}

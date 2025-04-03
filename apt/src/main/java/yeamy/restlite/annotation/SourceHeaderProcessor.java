package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.util.Set;

class SourceHeaderProcessor {
    public final TypeElement importType;
    public final ExecutableElement method;
    public final ElementKind kind;
    public final String name;
    public final String content;

    public SourceHeaderProcessor(ProcessEnvironment env, Element element, String name) {
        this.importType = (TypeElement) element.getEnclosingElement();
        this.name = name;
        ElementKind kind = this.kind = element.getKind();
        ExecutableElement method = (ExecutableElement) element;
        if (method.getParameters().size() > 0) {
            env.error("HeaderProcessor must be no parameter!");
            this.method = null;
            this.content = "null";
        } else if (kind == ElementKind.METHOD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)) {
                this.method = method;
                this.content = importType + "." + element.getSimpleName() + "()";
            } else {
                env.error("HeaderProcessor method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.method = null;
                this.content = "null";
            }
        } else {// CONSTRUCTOR
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.method = method;
                this.content = "new " + importType.getSimpleName() + "()";
            } else {
                env.error("HeaderProcessor constructor must have the modifier public:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.method = null;
                this.content = "null";
            }
        }
    }

}

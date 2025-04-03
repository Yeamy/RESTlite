package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.util.Set;

class SourceInjectProvider {
    public final TypeElement importType;
    public final ExecutableElement method;
    public final ElementKind kind;
    public final String name;
    public final String content;

    public SourceInjectProvider(ProcessEnvironment env, Element element, String name) {
        this.importType = (TypeElement) element.getEnclosingElement();
        this.name = name;
        ElementKind kind = this.kind = element.getKind();
        if (kind == ElementKind.FIELD) {
            this.method = null;
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)
                    && modifiers.contains(Modifier.FINAL)) {
                this.content = importType.getSimpleName() + "." + element.getSimpleName();
            } else {
                env.error("InjectProvider field must have the modifier public static final:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = "null";
            }
        } else {
            ExecutableElement method = (ExecutableElement) element;
            if (method.getParameters().size() > 0) {
                env.error("InjectProvider must be no parameter!");
                this.method = null;
                this.content = "null";
            } else if (kind == ElementKind.METHOD) {
                Set<Modifier> modifiers = element.getModifiers();
                if (modifiers.contains(Modifier.PUBLIC)
                        && modifiers.contains(Modifier.STATIC)) {
                    this.method = method;
                    this.content = importType + "." + element.getSimpleName() + "()";
                } else {
                    env.error("InjectProvider method must have the modifier public static:"
                            + element.asType().toString() + "." + element.getSimpleName());
                    this.method = null;
                    this.content = "null";
                }
            } else {// CONSTRUCTOR
                if (element.getModifiers().contains(Modifier.PUBLIC)) {
                    this.method = method;
                    this.content = "new " + importType.getSimpleName() + "()";
                } else {
                    env.error("InjectProvider constructor must have the modifier public:"
                            + element.asType().toString() + "." + element.getSimpleName());
                    this.method = null;
                    this.content = "null";
                }
            }
        }
    }

}

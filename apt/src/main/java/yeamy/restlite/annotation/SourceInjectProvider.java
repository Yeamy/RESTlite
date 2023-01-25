package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.util.Set;

class SourceInjectProvider {
    public final TypeElement importType;
    public final String type;
    private final Object[] content;

    public SourceInjectProvider(ProcessEnvironment env, Element element) {
        this.importType = (TypeElement) element.getEnclosingElement();
        ElementKind kind = element.getKind();
        if (kind == ElementKind.FIELD) {
            this.type = element.asType().toString();
        } else {
            this.type = ((ExecutableElement) element).getReturnType().toString();
        }
        if (element instanceof ExecutableElement
                && ((ExecutableElement) element).getParameters().size() > 0) {
            env.error("InjectProvider must have no parameter!");
            this.content = new String[]{"null;"};
        } else if (kind == ElementKind.FIELD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)
                    && modifiers.contains(Modifier.FINAL)) {
                this.content = new Object[]{importType, "." + element.getSimpleName() + ';'};
            } else {
                env.error("InjectProvider field must have the modifier public static final:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = new String[]{"null;"};
            }
        } else if (kind == ElementKind.METHOD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)) {
                this.content = new Object[]{importType, "." + element.getSimpleName() + "()"};
            } else {
                env.error("InjectProvider method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = new String[]{"null;"};
            }
        } else {// METHOD
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.content = new Object[]{"new ", importType, "()"};
            } else {
                env.error("InjectProvider constructor must have the modifier public:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = new String[]{"null;"};
            }
        }
    }

    public String create(SourceServlet servlet) {
        StringBuilder b = new StringBuilder();
        for (Object s : content) {
            b.append(s instanceof TypeElement ? servlet.imports((TypeElement) s) : s);
        }
        return b.toString();
    }
}

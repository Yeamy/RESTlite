package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Set;

class SourceInjectProvider {
    public final TypeElement importType;
    public final ArrayList<String> types = new ArrayList<>();
    private final String content;

    public SourceInjectProvider(ProcessEnvironment env, Element element) {
        this.importType = (TypeElement) element.getEnclosingElement();
        ElementKind kind = element.getKind();
        if (kind == ElementKind.FIELD) {
            this.types.add(element.asType().toString());
        } else {
            this.types.add(((ExecutableElement) element).getReturnType().toString());
        }
        InjectProvider ann = element.getAnnotation(InjectProvider.class);
        try {
            for (Class<?> t : ann.provideFor()) {
                this.types.add(t.getName());
            }
        } catch (MirroredTypesException e) {
            for (TypeMirror t : e.getTypeMirrors()) {
                this.types.add(t.toString());
            }
        }
        if (element instanceof ExecutableElement ee && ee.getParameters().size() > 0) {
            env.error("InjectProvider must have no parameter!");
            this.content = null;
        } else if (kind == ElementKind.FIELD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)
                    && modifiers.contains(Modifier.FINAL)) {
                this.content = importType.getSimpleName() + "." + element.getSimpleName() + ';';
            } else {
                env.error("InjectProvider field must have the modifier public static final:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = null;
            }
        } else if (kind == ElementKind.METHOD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)) {
                this.content = importType + "." + element.getSimpleName() + "()";
            } else {
                env.error("InjectProvider method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = null;
            }
        } else {// CONSTRUCTOR
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.content = "new " + importType.getSimpleName() + "()";
            } else {
                env.error("InjectProvider constructor must have the modifier public:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.content = null;
            }
        }
    }

    public String create(SourceServlet servlet) {
        if (content == null) {
            return "null;";
        }
        servlet.imports(importType);
        return content;
    }
}

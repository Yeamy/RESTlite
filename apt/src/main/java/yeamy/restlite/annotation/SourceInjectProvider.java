package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class SourceInjectProvider {
    public final TypeElement importType;
    public final TypeMirror outType;
    public final ExecutableElement method;
    public final ElementKind kind;
    public final String content;
    public final List<String> throwable;
    public final boolean closeable;
    public final boolean closeThrow;

    public SourceInjectProvider(ProcessEnvironment env, Element element) {
        this.importType = (TypeElement) element.getEnclosingElement();
        ElementKind kind = this.kind = element.getKind();
        if (kind == ElementKind.FIELD) {
            this.outType = element.asType();
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)
                    && modifiers.contains(Modifier.FINAL)) {
                this.method = null;
                this.content = importType.getSimpleName() + "." + element.getSimpleName();
                this.throwable = Collections.emptyList();
                this.closeable = this.closeThrow = false;
                return;
            } else {
                env.error("InjectProvider field must have the modifier public static final:"
                        + element.asType().toString() + "." + element.getSimpleName());
            }
        } else if (kind == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) element;
            this.outType = ((ExecutableElement) element).getReturnType();
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC) && modifiers.contains(Modifier.STATIC)) {
                this.method = method;
                this.content = importType + "." + element.getSimpleName() + "()";
                this.throwable = ProcessEnvironment.getThrowType(method);
                this.closeable = env.isCloseable(outType);
                this.closeThrow = closeable && ProcessEnvironment.isCloseThrow(env.getTypeElement(outType.toString()));
                return;
            } else if (method.getParameters().size() > 0) {
                env.error("InjectProvider must be no parameter!");
            } else {
                env.error("InjectProvider method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
            }
        } else {// CONSTRUCTOR
            ExecutableElement method = (ExecutableElement) element;
            Element type = element.getEnclosingElement();
            this.outType = type.asType();
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.method = method;
                this.content = "new " + importType.getSimpleName() + "()";
                this.throwable = ProcessEnvironment.getThrowType(method);
                this.closeable = env.isCloseable(outType);
                this.closeThrow = closeable && ProcessEnvironment.isCloseThrow((TypeElement) type);
                return;
            } else if (method.getParameters().size() > 0) {
                env.error("InjectProvider must be no parameter!");
            } else {
                env.error("InjectProvider constructor must have the modifier public:"
                        + element.asType().toString() + "." + element.getSimpleName());
            }
        }
        this.method = null;
        this.content = "null";
        this.throwable = Collections.emptyList();
        this.closeable = this.closeThrow = false;
    }

}

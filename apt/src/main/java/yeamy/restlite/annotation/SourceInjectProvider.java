package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.util.Set;

class SourceInjectProvider {
    public final TypeElement importType;
    public final ExecutableElement method;
    public final ElementKind kind;
    public final String content;
    public final boolean throwable;
    public final boolean closeable;
    public final boolean closeThrow;

    public SourceInjectProvider(ProcessEnvironment env, Element element) {
        this.importType = (TypeElement) element.getEnclosingElement();
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
            this.throwable = closeable = closeThrow = false;
        } else {
            ExecutableElement method = (ExecutableElement) element;
            if (method.getParameters().size() > 0) {
                env.error("InjectProvider must be no parameter!");
                this.method = null;
                this.content = "null";
                this.throwable = closeable = closeThrow = false;
            } else if (kind == ElementKind.METHOD) {
                Set<Modifier> modifiers = element.getModifiers();
                if (modifiers.contains(Modifier.PUBLIC) && modifiers.contains(Modifier.STATIC)) {
                    this.method = method;
                    this.content = importType + "." + element.getSimpleName() + "()";
                    this.throwable = method.getThrownTypes().size() > 0;
                    this.closeable = env.isCloseable(method.getReturnType());
                    this.closeThrow = closeable && ProcessEnvironment.isCloseThrow(importType);
                } else {
                    env.error("InjectProvider method must have the modifier public static:"
                            + element.asType().toString() + "." + element.getSimpleName());
                    this.method = null;
                    this.content = "null";
                    this.throwable = closeable = closeThrow = false;
                }
            } else {// CONSTRUCTOR
                if (element.getModifiers().contains(Modifier.PUBLIC)) {
                    this.method = method;
                    this.content = "new " + importType.getSimpleName() + "()";
                    this.throwable = method.getThrownTypes().size() > 0;
                    this.closeable = env.isCloseable(method.getReturnType());
                    this.closeThrow = closeable && ProcessEnvironment.isCloseThrow(importType);
                } else {
                    env.error("InjectProvider constructor must have the modifier public:"
                            + element.asType().toString() + "." + element.getSimpleName());
                    this.method = null;
                    this.content = "null";
                    this.throwable = closeable = closeThrow = false;
                }
            }
        }
    }

}

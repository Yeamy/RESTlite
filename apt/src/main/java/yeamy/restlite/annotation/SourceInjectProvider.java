package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class SourceInjectProvider {
    private final TypeElement importType;
    private final ElementKind kind;
    private final ExecutableElement method;
    private final VariableElement field;
    public final TypeMirror outType;
    public final List<String> throwable;
    public final boolean closeable;
    public final boolean closeThrow;

    public SourceInjectProvider(ProcessEnvironment env, Element element) {
        ElementKind kind = this.kind = element.getKind();
        if (kind == ElementKind.FIELD) {
            this.outType = element.asType();
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC)
                    && modifiers.contains(Modifier.STATIC)
                    && modifiers.contains(Modifier.FINAL)) {
                this.importType = (TypeElement) element.getEnclosingElement();
                this.method = null;
                this.field = (VariableElement) element;
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
                this.importType = (TypeElement) element.getEnclosingElement();
                this.method = method;
                this.field = null;
                this.throwable = ProcessEnvironment.getThrowType(method);
                this.closeable = env.isCloseable(outType);
                this.closeThrow = closeable && ProcessEnvironment.isCloseThrow(env.getTypeElement(outType.toString()));
                return;
            } else {
                env.error("InjectProvider method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
            }
        } else {// CONSTRUCTOR
            ExecutableElement method = (ExecutableElement) element;
            Element type = element.getEnclosingElement();
            this.outType = type.asType();
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.importType = (TypeElement) element.getEnclosingElement();
                this.method = method;
                this.field = null;
                this.throwable = ProcessEnvironment.getThrowType(method);
                this.closeable = env.isCloseable(outType);
                this.closeThrow = closeable && ProcessEnvironment.isCloseThrow((TypeElement) type);
                return;
            } else {
                env.error("InjectProvider constructor must have the modifier public:"
                        + element.asType().toString() + "." + element.getSimpleName());
            }
        }
        this.importType = null;
        this.method = null;
        this.field = null;
        this.throwable = Collections.emptyList();
        this.closeable = this.closeThrow = false;
    }

    public boolean isFail() {
        return method == null && field == null;
    }

    public void write(StringBuilder b, SourceServlet servlet) {
        if (field != null) {
            b.append(servlet.imports(importType.asType())).append('.').append(field.getSimpleName());
        } else if (method == null) {
            b.append("null");
        } else if (kind == ElementKind.METHOD) {
            b.append(servlet.imports(importType.asType())).append('.').append(method.getSimpleName());
            SourceInject.writeParam(b, method);
        } else {
            b.append("new ").append(servlet.imports(importType.asType()));
            SourceInject.writeParam(b, method);
        }

    }
}

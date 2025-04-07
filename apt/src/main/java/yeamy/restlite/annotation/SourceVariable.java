package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

abstract class SourceVariable {
    protected final ProcessEnvironment env;
    protected final VariableElement param;
    private boolean throwable = false;
    private boolean closeable = false;
    private boolean closeThrow = false;

    public SourceVariable(ProcessEnvironment env, VariableElement param) {
        this.env = env;
        this.param = param;
    }

    protected void init(boolean throwable, boolean closeable, boolean closeThrow) {
        this.throwable = throwable;
        this.closeable = closeable;
        this.closeThrow = closeThrow;
    }

    protected void init(ExecutableElement method, TypeMirror classType, boolean samePackage, List<? extends Element> elements) {
        if (method != null) {
            this.throwable = method.getThrownTypes().size() > 0;
        }
        this.closeable = env.isCloseable(classType);
        if (closeable) {
            for (Element element : elements) { // find close method
                if (element.getKind() == ElementKind.METHOD//
                        && element.getSimpleName().toString().equals("close")) {
                    Set<Modifier> modifiers = element.getModifiers();
                    if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)) {
                        continue;
                    }
                    if (samePackage || modifiers.contains(Modifier.PUBLIC)) {
                        ExecutableElement close = (ExecutableElement) element;
                        if (close.getParameters().size() == 0) {
                            this.closeThrow = close.getThrownTypes().size() > 0;
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isCloseable() {
        return closeable;
    }

    public boolean isCloseThrow() {
        return closeThrow;
    }

    public boolean isThrowable() {
        return throwable;
    }


    public void writeClose(StringBuilder b, SourceServlet servlet) {
        b.append(servlet.imports("yeamy.utils.StreamUtils")).append(".close(").append(param.getSimpleName()).append(");");
    }
}

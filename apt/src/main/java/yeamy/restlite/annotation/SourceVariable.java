package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Set;

abstract class SourceVariable {
    protected final ProcessEnvironment env;
    protected final VariableElement param;
    private List<String> throwable = Collections.emptyList();
    private boolean closeable = false;
    private boolean closeThrow = false;

    public SourceVariable(ProcessEnvironment env, VariableElement param) {
        this.env = env;
        this.param = param;
    }

    protected void init(List<String> throwable, boolean closeable, boolean closeThrow) {
        this.throwable = throwable;
        this.closeable = closeable;
        this.closeThrow = closeThrow;
    }

    protected void init(ExecutableElement method, TypeMirror classType, boolean samePackage, List<? extends Element> elements) {
        if (method != null) {
            this.throwable = ProcessEnvironment.getThrowType(method);
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
                        if (close.getParameters().isEmpty()) {
                            this.closeThrow = !close.getThrownTypes().isEmpty();
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
        return !throwable.isEmpty();
    }

    public void writeCloseField(StringBuilder b, SourceServlet servlet) {
        if (!param.getModifiers().contains(Modifier.PRIVATE)) {
            b.append(servlet.imports("yeamy.restlite.utils.StreamUtils")).append(".close(_impl.").append(param.getSimpleName()).append(");");
            return;
        }
        ExecutableElement getter = env.findGetter(param);
        if (getter != null) {
            b.append(servlet.imports("yeamy.restlite.utils.StreamUtils")).append(".close(_impl.").append(getter.getSimpleName()).append("());");
            return;
        }
        env.error("Cannot assign " + servlet.getImpl() + "." + param.getSimpleName() + " cause it's private and no getter found");
    }

    public List<String> throwTypes() {
        return throwable;
    }
}

package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

class SourceInjectByExecutable extends SourceInject {
    private final TypeElement classType;
    private final ExecutableElement method;
    private final TypeMirror returnType;

    SourceInjectByExecutable(ProcessEnvironment env,
                             VariableElement param,
                             TypeElement classType,
                             ExecutableElement method,
                             TypeMirror returnType,
                             boolean samePackage,
                             List<? extends Element> elements) {
        super(env, param);
        this.classType = classType;
        this.method = method;
        this.returnType = returnType;
        init(method, classType.asType(), samePackage, elements);
    }

    @Override
    public CharSequence writeArg(SourceServlet servlet) {
        String typeName = servlet.imports(returnType);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(param.getSimpleName()).append(" = ");
        if (method.getKind().equals(ElementKind.CONSTRUCTOR)) {
            b.append("new ").append(typeName).append("()");
        } else {
            b.append(servlet.imports(classType)).append('.').append(method.getSimpleName()).append("()");
        }
        b.append(';');
        return b;
    }

    @Override
    protected void writeFieldValue(StringBuilder b, SourceServlet servlet) {
        if (method.getKind().equals(ElementKind.CONSTRUCTOR)) {
            b.append("new ").append(servlet.imports(returnType)).append("()");
        } else {
            b.append(servlet.imports(classType)).append('.').append(method.getSimpleName()).append("()");
        }
    }
}

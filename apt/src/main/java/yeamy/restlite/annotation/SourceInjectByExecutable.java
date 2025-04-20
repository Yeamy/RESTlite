package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

class SourceInjectByExecutable extends SourceInject {
    private final TypeElement classType;
    private final ExecutableElement method;

    SourceInjectByExecutable(ProcessEnvironment env,
                             VariableElement param,
                             TypeElement classType,
                             ExecutableElement method,
                             TypeMirror returnType,
                             boolean samePackage,
                             List<? extends Element> elements) {
        super(env, param, returnType);
        this.classType = classType;
        this.method = method;
        init(method, classType.asType(), samePackage, elements);
    }

    @Override
    protected void writeCreator(StringBuilder b, SourceServlet servlet) {
        if (method.getKind().equals(ElementKind.CONSTRUCTOR)) {
            b.append("new ").append(servlet.imports(returnType));
        } else {
            b.append(servlet.imports(classType)).append('.').append(method.getSimpleName());
        }
        writeParam(b, method);
    }
}

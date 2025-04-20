package yeamy.restlite.annotation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class SourceInjectByField extends SourceInject {
    private final TypeElement classType;
    private final VariableElement field;

    SourceInjectByField(ProcessEnvironment env,
                        VariableElement param,
                        TypeElement classType,
                        VariableElement field,
                        TypeMirror returnType) {
        super(env, param, returnType);
        this.classType = classType;
        this.field = field;
    }

    @Override
    protected void writeCreator(StringBuilder b, SourceServlet servlet) {
        b.append(servlet.imports(classType)).append('.').append(field.getSimpleName());
    }
}

package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class SourceInjectNull extends SourceInject {

    SourceInjectNull(ProcessEnvironment env,
                     VariableElement param,
                     TypeMirror returnType) {
        super(env, param, returnType);
    }

    @Override
    protected void writeCreator(StringBuilder b, SourceServlet servlet) {
        b.append("null");
    }
}

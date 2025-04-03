package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class SourceInjectNull extends SourceInject {
    private final TypeMirror returnType;

    SourceInjectNull(ProcessEnvironment env,
                     VariableElement param,
                     TypeMirror returnType) {
        super(env, param);
        this.returnType = returnType;
    }

    @Override
    public CharSequence writeArg(SourceServlet servlet) {
        String typeName = servlet.imports(returnType);
        return new StringBuilder(typeName).append(" ").append(param.getSimpleName()).append(" = null;");
    }

    @Override
    public void writeValue(StringBuilder b, SourceServlet servlet) {
        b.append("null");
    }
}

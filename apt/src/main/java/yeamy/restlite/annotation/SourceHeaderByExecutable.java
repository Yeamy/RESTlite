package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceHeaderByExecutable extends SourceHeader {
    private final TypeElement classType;
    private final ExecutableElement method;
    private final TypeMirror returnType;

    SourceHeaderByExecutable(ProcessEnvironment env,
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
    public CharSequence write(SourceServlet servlet, String name, String alias) {
        String typeName = servlet.imports(returnType);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(alias).append(" = ");
        if (method.getKind().equals(ElementKind.CONSTRUCTOR)) {
            b.append("new ").append(typeName);
        } else {
            b.append(servlet.imports(classType)).append('.').append(method.getSimpleName());
        }
        switch (method.getParameters().get(0).asType().toString()) {
            case T_int, T_Integer -> b.append("(_req.getIntHeader(\"").append(name).append("\"));");
            case T_long, T_Long -> b.append("(_req.getDateHeader(\"").append(name).append("\"));");
            case T_String -> b.append("(_req.getHeader(\"").append(name).append("\"));");
            case T_Date -> b.append("(new ").append(servlet.imports(T_Date)).append("(_req.getDateHeader(\"")
                    .append(name).append("\"));");
        }
        return b;
    }

}

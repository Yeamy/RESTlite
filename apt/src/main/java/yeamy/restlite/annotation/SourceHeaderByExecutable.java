package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.T_Date;
import static yeamy.restlite.annotation.SupportType.T_String;

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
        b.append('(').append(writeArgument(servlet, name)).append(");");
        return b;
    }

    private String writeArgument(SourceServlet servlet, String name) {
        TypeKind kind = returnType.getKind();
        if (kind.equals(TypeKind.INT)) {
            return "_req.getIntHeader(\"" + name + "\");";
        } else if (kind.equals(TypeKind.LONG)) {
            return "_req.getDateHeader(\"" + name + "\");";
        }
        String type = returnType.toString();
        if (T_String.equals(type)) {
            return "_req.getHeader(\"" + name + "\");";
        } else if (T_Date.equals(type)) {
            return "new " + servlet.imports(T_Date) + "(_req.getDateHeader(\"" + name + "\"));";
        }
        return "";
    }
}

package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceCookieByExecutable extends SourceCookie {
    private final TypeElement classType;
    private final ExecutableElement method;
    private final TypeMirror returnType;

    SourceCookieByExecutable(ProcessEnvironment env, VariableElement param, SourceCookieProcessor p) {
        super(env, param);
        this.classType = p.classType;
        this.method = p.method;
        this.returnType = p.returnType;
        init(p.throwable, p.closeable, p.closeThrow);
    }

    SourceCookieByExecutable(ProcessEnvironment env,
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
            case T_String -> b.append("(_req.getCookieValue(\"").append(name).append("\");");
            case T_Cookie -> b.append("(_req.getCookie(\"").append(name).append("\");");
            case T_CookieArray -> b.append("(_req.getCookies();");
        }
        return b;
    }

}

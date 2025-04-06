package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceBodyByExecutable extends SourceBody {
    private final TypeElement classType;
    private final ExecutableElement method;
    private final TypeMirror returnType;

    SourceBodyByExecutable(ProcessEnvironment env,
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
    public CharSequence write(SourceServlet servlet, String name) {
        String typeName = servlet.imports(returnType);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(name).append(" = ");
        if (method.getKind().equals(ElementKind.CONSTRUCTOR)) {
            b.append("new ").append(typeName);
        } else {
            b.append(servlet.imports(classType)).append('.').append(method.getSimpleName());
        }
        b.append('(');
        final String CLASS = "java.lang.Class<" + returnType + ">";
        boolean append = false;
        for (VariableElement p : method.getParameters()) {
            if (append) {
                b.append(", ");
            } else {
                append = true;
            }
            TypeMirror tm = p.asType();
            String type = tm.toString();
            if (tm.getKind().equals(TypeKind.TYPEVAR) && CLASS.equals(type)) {
                b.append(typeName).append(".class");
                continue;
            }
            switch (type) {
                case T_InputStream, T_ServletInputStream -> b.append("_req.getBody()");
                case T_ByteArray -> b.append("_req.getBodyAsByte()");
                case T_String -> b.append("_req.getBodyAsText(_req.getCharset())");
                case T_PartArray -> b.append("_req.getParts()");
                case T_FileArray -> b.append("_req.getFiles()");
                case T_Charset -> b.append("_req.getCharset()");
            }
        }
        b.append(");");
        return b;
    }

}

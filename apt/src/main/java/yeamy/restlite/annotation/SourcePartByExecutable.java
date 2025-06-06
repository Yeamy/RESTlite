package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourcePartByExecutable extends SourcePart {
    private final TypeElement classType;
    private final ExecutableElement method;
    private final TypeMirror returnType;

    SourcePartByExecutable(ProcessEnvironment env, VariableElement param, SourcePartProcessor p) {
        super(env, param);
        this.classType = p.classType;
        this.method = p.method;
        this.returnType = p.returnType;
        init(p.throwable, p.closeable, p.closeThrow);
    }

    SourcePartByExecutable(ProcessEnvironment env,
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
        boolean isTypeVar = returnType.getKind().equals(TypeKind.TYPEVAR);
        String typeName = servlet.imports(isTypeVar ? param.asType() : returnType);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(alias).append(" = ");
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
            if (isTypeVar) {
                if (CLASS.equals(type) || T_CLASS.equals(type)) {
                    if (typeName.contains("<")) {
                        env.error("Unable to generate code with generics of method:" + method.getSimpleName() + " arg:" + p.asType() + " " + p.getSimpleName());
                        b.append("/*Unable to generate code with generics.*/null");
                    } else {
                        b.append(typeName).append(".class");
                    }
                    continue;
                } else if (T_Type.equals(type)) {
                    if (typeName.contains("<")) {
                        b.append("new ").append(servlet.imports("yeamy.restlite.utils.TypeUtils")).append('<').append(returnType).append(">(){}.getType()");
                    } else {
                        b.append(typeName).append(".class");
                    }
                    continue;
                }
            }
            switch (type) {
                case T_Part -> b.append("_req.getPart(\"").append(name).append("\")");
                case T_HttpRequestFile -> b.append("_req.getFile(\"").append(name).append("\")");
                case T_InputStream -> b.append(servlet.imports(T_IfNotNull))
                        .append(".invoke(_req.getFile(\"").append(name).append("\"),a->a.get())");
                case T_ByteArray -> b.append(servlet.imports(T_IfNotNull))
                        .append(".invoke(_req.getFile(\"").append(name).append("\"),a->a.getAsByte())");
                case T_String -> b.append(servlet.imports(T_IfNotNull))
                        .append(".invoke(_req.getFile(\"").append(name).append("\"),a->a.getAsText())");
                default -> b.append(ProcessEnvironment.inValidTypeValue(tm));
            }
        }
        b.append(");");
        return b;
    }

}

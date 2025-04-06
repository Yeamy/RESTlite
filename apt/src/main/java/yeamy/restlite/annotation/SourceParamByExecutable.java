package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceParamByExecutable extends SourceParam {
    private final TypeElement classType;
    private final ExecutableElement method;
    private final TypeMirror returnType;

    SourceParamByExecutable(ProcessEnvironment env,
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
            case T_int -> b.append("(_req.getIntParam(\"").append(name).append("\", 0));");
            case T_Integer -> b.append("(_req.getIntParam(\"").append(name).append("\"));");
            case T_IntegerArray -> b.append("(_req.getIntegerParams(\"").append(name).append("\"));");
            case T_long -> b.append("(_req.getLongParam(\"").append(name).append("\", 0L));");
            case T_Long -> b.append("(_req.getLongParam(\"").append(name).append("\"));");
            case T_LongArray -> b.append("(_req.getLongParams(\"").append(name).append("\"));");
            case T_float -> b.append("(_req.getFloatParam(\"").append(name).append("\", 0F));");
            case T_Float -> b.append("(_req.getFloatParam(\"").append(name).append("\"));");
            case T_FloatArray -> b.append("(_req.getFloatParams(\"").append(name).append("\"));");
            case T_double -> b.append("(_req.getDoubleParam(\"").append(name).append("\", 0D));");
            case T_Double -> b.append("(_req.getDoubleParam(\"").append(name).append("\"));");
            case T_DoubleArray -> b.append("(_req.getDoubleParams(\"").append(name).append("\"));");
            case T_boolean -> b.append("(_req.getBooleanParam(\"").append(name).append("\", false));");
            case T_Boolean -> b.append("(_req.getBooleanParam(\"").append(name).append("\"));");
            case T_BooleanArray -> b.append("(_req.getBooleanParams(\"").append(name).append("\"));");
            case T_Decimal -> b.append("(_req.getDecimalParam(\"").append(name).append("\"));");
            case T_DecimalArray -> b.append("(_req.getDecimalParams(\"").append(name).append("\"));");
            case T_String -> b.append("(_req.getParam(\"").append(name).append("\"));");
            case T_StringArray -> b.append("(_req.getParams(\"").append(name).append("\"));");
        }
        return b;
    }

}

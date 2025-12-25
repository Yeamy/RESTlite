package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

import static yeamy.restlite.annotation.SupportType.*;

class SourceParamDefault extends SourceParam {
    private final String returnType;

    SourceParamDefault(ProcessEnvironment env, VariableElement param, String returnType) {
        super(env, param);
        this.returnType = returnType;
    }

    @Override
    public CharSequence write(SourceServlet servlet, String name, String alias) {
        return switch (returnType) {
            case T_int -> "int " + alias + " = _req.getIntParam(\"" + name + "\", 0);";
            case T_Integer -> "Integer " + alias + " = _req.getIntParam(\"" + name + "\");";
            case T_IntegerArray -> "Integer[] " + alias + " = _req.getIntParams(\"" + name + "\");";
            case T_long -> "long " + alias + " = _req.getLongParam(\"" + name + "\", 0L);";
            case T_Long -> "Long " + alias + " = _req.getLongParam(\"" + name + "\");";
            case T_LongArray -> "Long[] " + alias + " = _req.getLongParams(\"" + name + "\");";
            case T_float -> "float " + alias + " = _req.getFloatParam(\"" + name + "\", 0F);";
            case T_Float -> "Float " + alias + " = _req.getFloatParam(\"" + name + "\");";
            case T_FloatArray -> "Float[] " + alias + " = _req.getFloatParams(\"" + name + "\");";
            case T_double -> "double " + alias + " = _req.getDoubleParam(\"" + name + "\", 0D);";
            case T_Double -> "Double " + alias + " = _req.getDoubleParam(\"" + name + "\");";
            case T_DoubleArray -> "Double[] " + alias + " = _req.getDoubleParams(\"" + name + "\");";
            case T_boolean -> "boolean " + alias + " = _req.getBooleanParam(\"" + name + "\", false);";
            case T_Boolean -> "Boolean " + alias + " = _req.getBooleanParam(\"" + name + "\");";
            case T_BooleanArray -> "Boolean[] " + alias + " = _req.getBooleanParams(\"" + name + "\");";
            case T_Decimal -> "BigDecimal " + alias + " = _req.getDecimalParam(\"" + name + "\");";
            case T_DecimalArray -> "BigDecimal[] " + alias + " = _req.getDecimalParams(\"" + name + "\");";
            case T_String -> "String " + alias + " = _req.getParam(\"" + name + "\");";
            case T_StringArray -> "String[] " + alias + " = _req.getParams(\"" + name + "\");";
            default -> null;// never go here
        };
    }

}

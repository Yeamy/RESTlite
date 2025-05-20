package yeamy.restlite.annotation;

import yeamy.restlite.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

class SourceParamProcessor {
    static final String[] SUPPORT_PARAM_TYPE = new String[]{
            T_String, T_StringArray,
            T_int, T_Integer, T_IntegerArray,
            T_long, T_Long, T_LongArray,
            T_float, T_Float, T_FloatArray,
            T_double, T_Double, T_DoubleArray,
            T_boolean, T_Boolean, T_BooleanArray,
            T_Decimal, T_DecimalArray};

    public final TypeElement classType;
    public final ExecutableElement method;
    public final TypeMirror returnType;
    public final ElementKind kind;
    public final List<String> throwable;
    public final boolean closeable;
    public final boolean closeThrow;

    public SourceParamProcessor(ProcessEnvironment env, Element element) {
        this.classType = (TypeElement) element.getEnclosingElement();
        ElementKind kind = this.kind = element.getKind();
        ExecutableElement method = (ExecutableElement) element;
        this.returnType = kind.equals(ElementKind.METHOD)
                ? method.getReturnType()
                : method.getEnclosingElement().asType();
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() != 1 || TextUtils.notIn(parameters.get(0).asType().toString(), SUPPORT_PARAM_TYPE)) {
            env.error("ParamProcessor " + element.getSimpleName() + " contains unsupported param type!");
            this.method = null;
            this.throwable = Collections.emptyList();
            this.closeable = this.closeThrow = false;
        } else if (kind == ElementKind.METHOD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC) && modifiers.contains(Modifier.STATIC)) {
                this.method = method;
                this.throwable = ProcessEnvironment.getThrowType(method);
                this.closeable = env.isCloseable(method.getReturnType());
                this.closeThrow = closeable && ProcessEnvironment.isCloseThrow(classType);
            } else {
                env.error("ParamProcessor method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.method = null;
                this.throwable = Collections.emptyList();
                this.closeable = this.closeThrow = false;
            }
        } else {// CONSTRUCTOR
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.method = method;
                this.throwable = ProcessEnvironment.getThrowType(method);
                this.closeable = env.isCloseable(method.getReturnType());
                this.closeThrow = closeable && ProcessEnvironment.isCloseThrow(classType);
            } else {
                env.error("ParamProcessor constructor must have the modifier public:"
                        + classType + "." + element.getSimpleName());
                this.method = null;
                this.throwable = Collections.emptyList();
                this.closeable = this.closeThrow = false;
            }
        }
    }

}

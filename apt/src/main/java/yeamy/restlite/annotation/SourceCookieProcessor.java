package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

class SourceCookieProcessor {
    static final String[] SUPPORT_COOKIE_TYPE = new String[]{T_String, T_Cookie, T_CookieArray};

    public final TypeElement classType;
    public final ExecutableElement method;
    public final TypeMirror returnType;
    public final ElementKind kind;

    public SourceCookieProcessor(ProcessEnvironment env, Element element) {
        this.classType = (TypeElement) element.getEnclosingElement();
        ElementKind kind = this.kind = element.getKind();
        ExecutableElement method = (ExecutableElement) element;
        this.returnType = method.getReturnType();
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() != 1 || TextUtils.notIn(parameters.get(0).asType().toString(), SUPPORT_COOKIE_TYPE)) {
            env.error("CookieProcessor " + element.getSimpleName() + " contains unsupported param type!");
            this.method = null;
        } else if (kind == ElementKind.METHOD) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers.contains(Modifier.PUBLIC) && modifiers.contains(Modifier.STATIC)) {
                this.method = method;
            } else {
                env.error("CookieProcessor method must have the modifier public static:"
                        + element.asType().toString() + "." + element.getSimpleName());
                this.method = null;
            }
        } else {// CONSTRUCTOR
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                this.method = method;
            } else {
                env.error("CookieProcessor constructor must have the modifier public:"
                        + classType + "." + element.getSimpleName());
                this.method = null;
            }
        }
    }

}

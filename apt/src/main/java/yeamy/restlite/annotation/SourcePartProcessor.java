package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

class SourcePartProcessor {
    static final String[] SUPPORT_PART_TYPE = new String[]{T_Part, T_File, T_InputStream, T_ByteArray, T_String};

    public final TypeElement classType;
    public final ExecutableElement method;
    public final TypeMirror returnType;

    public SourcePartProcessor(ProcessEnvironment env, Element element) {
        this.classType = (TypeElement) element.getEnclosingElement();
        ExecutableElement method = (ExecutableElement) element;
        this.returnType = method.getReturnType();
        Set<Modifier> modifiers = element.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC)) {
            env.error("PartProcessor must be public:" + classType + "." + element.getSimpleName());
            this.method = null;
            return;
        }
        if (element.getKind() == ElementKind.METHOD && !modifiers.contains(Modifier.STATIC)) {
            env.error("PartProcessor must be static:" + classType + "." + element.getSimpleName());
            this.method = null;
            return;
        }
        if (checkParam(returnType, method.getParameters())) {
            this.method = method;
        } else {
            env.error("PartProcessor has invalid param type:" + classType + "." + element.getSimpleName());
            this.method = null;
        }
    }

    public static boolean checkParam(TypeMirror returnType, List<? extends VariableElement> parameters) {
        final String CLASS = "java.lang.Class<" + returnType + ">";
        boolean checkType = returnType.getKind().equals(TypeKind.TYPEVAR);
        int pType = checkType ? 0 : 1, pInput = 0, pOthers = 0;
        for (VariableElement p : parameters) {
            TypeMirror pt = p.asType();
            String pts = pt.toString();
            if (checkType && pt.getKind().equals(TypeKind.TYPEVAR)) {
                if (!pts.equals(CLASS)) {
                    return false;
                }
                pType++;
            } else if (TextUtils.in(pts, SUPPORT_PART_TYPE)) {
                pInput++;
            } else {
                pOthers++;
            }
        }
        return pType == 1 && pInput == 1 && pOthers == 0;
    }
}

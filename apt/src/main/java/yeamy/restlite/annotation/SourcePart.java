package yeamy.restlite.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

abstract class SourcePart extends SourceVariable {
    private final TypeMirror typeMirror;

    public SourcePart(ProcessEnvironment env, VariableElement param) {
        super(env, param);
        this.typeMirror = param.getEnclosingElement().asType();
    }

    protected boolean isAssignable(Element e, Set<Modifier> modifiers) {
        return modifiers.contains(Modifier.PUBLIC)
                || (!modifiers.contains(Modifier.PRIVATE) && env.isAssignable(typeMirror, e.asType()));
    }

    public abstract CharSequence write(SourceServlet servlet, String name, String alias);

}

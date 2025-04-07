package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

abstract class SourcePart extends SourceVariable {

    public SourcePart(ProcessEnvironment env, VariableElement param) {
        super(env, param);
    }

    public abstract CharSequence write(SourceServlet servlet, String name, String alias);

}

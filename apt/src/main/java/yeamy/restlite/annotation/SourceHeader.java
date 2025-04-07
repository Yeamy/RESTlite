package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

abstract class SourceHeader extends SourceVariable {

    public SourceHeader(ProcessEnvironment env, VariableElement param) {
        super(env, param);
    }

    public abstract CharSequence write(SourceServlet servlet, String name, String alias);

}

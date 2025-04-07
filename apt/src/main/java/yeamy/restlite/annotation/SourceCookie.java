package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

abstract class SourceCookie extends SourceVariable {

    public SourceCookie(ProcessEnvironment env, VariableElement param) {
        super(env, param);
    }

    public abstract CharSequence write(SourceServlet servlet, String name, String alias);

}

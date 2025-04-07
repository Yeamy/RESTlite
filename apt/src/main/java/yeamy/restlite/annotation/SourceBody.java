package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

abstract class SourceBody extends SourceVariable {

    public SourceBody(ProcessEnvironment env, VariableElement param) {
        super(env, param);
    }

    public abstract CharSequence write(SourceServlet servlet, String name);

}

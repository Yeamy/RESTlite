package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

class SourceParamInject extends SourceParam {
    private final VariableElement param;

    public SourceParamInject(ProcessEnvironment env, VariableElement param) {
        super(env, env.getTypeElement(param.asType().toString()), false);
        this.param = param;
    }

    @Override
    protected String declaredArgument(SourceServlet servlet, VariableElement param) {
        return "null";
    }

    @Override
    public CharSequence toCharSequence(SourceServlet servlet, SourceArguments args, String name) {
        StringBuilder b = new StringBuilder();
        new SourceInject(servlet, param).createParameter(b);
        return b;
    }

}

package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

class SourceParamFail extends SourceParamCreator {

    final static SourceParamFail INSTANCE = new SourceParamFail();

    private SourceParamFail() {
        super(null, null);
    }

    @Override
    protected String declaredArgument(SourceServlet servlet, VariableElement param) {
        return null;
    }

    @Override
    public CharSequence toCharSequence(SourceServlet servlet, SourceArguments args, String name) {
        return "";
    }

}

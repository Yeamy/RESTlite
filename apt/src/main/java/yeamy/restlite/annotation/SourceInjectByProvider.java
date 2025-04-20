package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

class SourceInjectByProvider extends SourceInject {
    private final SourceInjectProvider injectProvider;

    SourceInjectByProvider(ProcessEnvironment env, VariableElement param, SourceInjectProvider p) {
        super(env, param, p.outType);
        this.injectProvider = p;
        init(p.throwable, p.closeable, p.closeThrow);
    }

    @Override
    protected void writeCreator(StringBuilder b, SourceServlet servlet) {
        injectProvider.write(b, servlet);
    }
}

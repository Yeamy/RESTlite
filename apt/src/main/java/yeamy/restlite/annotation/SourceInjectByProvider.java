package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

class SourceInjectByProvider extends SourceInject {
    private final SourceInjectProvider injectProvider;

    SourceInjectByProvider(ProcessEnvironment env, VariableElement param, SourceInjectProvider p) {
        super(env, param);
        this.injectProvider = p;
        init(p.throwable, p.closeable, p.closeThrow);
    }

    @Override
    public CharSequence writeArg(SourceServlet servlet) {
        return new StringBuilder().append(servlet.imports(injectProvider.importType)).append(' ')
                .append(param.getSimpleName()).append('=').append(injectProvider.content).append(';');
    }

    @Override
    protected void writeFieldValue(StringBuilder b, SourceServlet servlet) {
        servlet.imports(injectProvider.importType);
        b.append(injectProvider.content);
    }
}

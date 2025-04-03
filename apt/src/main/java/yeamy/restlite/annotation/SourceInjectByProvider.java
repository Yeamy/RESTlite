package yeamy.restlite.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.List;

class SourceInjectByProvider extends SourceInject {
    private final SourceInjectProvider injectProvider;

    SourceInjectByProvider(ProcessEnvironment env,
                           VariableElement param,
                           SourceInjectProvider injectProvider,
                           boolean samePackage,
                           List<? extends Element> elements) {
        super(env, param);
        this.injectProvider = injectProvider;
        init(injectProvider.method, injectProvider.importType.asType(), samePackage, elements);
    }

    @Override
    public void writeValue(StringBuilder b, SourceServlet servlet) {
        b.append(injectProvider.content);
    }

    @Override
    public CharSequence writeArg(SourceServlet servlet) {
        StringBuilder b = new StringBuilder().append(servlet.imports(injectProvider.importType)).append(' ')
                .append(param.getSimpleName()).append('=');
        writeValue(b, servlet);
        b.append(';');
        return b;
    }
}

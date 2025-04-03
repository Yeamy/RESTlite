package yeamy.restlite.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.List;

class SourceHeaderByProcessor extends SourceHeader {
    private final SourceHeaderProcessor processor;

    SourceHeaderByProcessor(ProcessEnvironment env,
                            VariableElement param,
                            SourceHeaderProcessor processor,
                            boolean samePackage,
                            List<? extends Element> elements) {
        super(env, param);
        this.processor = processor;
        init(processor.method, processor.importType.asType(), samePackage, elements);
    }

    @Override
    public CharSequence write(SourceServlet servlet, String name, String alias) {
        return new StringBuilder().append(servlet.imports(processor.importType)).append(' ')
                .append(param.getSimpleName()).append('=').append(processor.content).append(';');
    }
}

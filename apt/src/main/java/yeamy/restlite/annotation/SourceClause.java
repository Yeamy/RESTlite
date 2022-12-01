package yeamy.restlite.annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Yeamy
 * @see SourceDispatchService
 * @see SourceDispatchOnError
 */
abstract class SourceClause {
    protected final ProcessEnvironment env;
    protected final SourceServlet servlet;
    protected final ExecutableElement method;
    protected final CharSequence name;
    protected final List<? extends VariableElement> arguments;
    private String orderKey;

    public SourceClause(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method) {
        this.env = env;
        this.servlet = servlet;
        this.method = method;
        this.name = method.getSimpleName();
        this.arguments = this.method.getParameters();
    }

    final String orderKey() {
        if (orderKey != null) {
            return orderKey;
        }
        TreeSet<String> set = new TreeSet<>();
        for (VariableElement a : arguments) {
            Param pa = a.getAnnotation(Param.class);
            if (pa != null && pa.required()) {
                set.add(a.getSimpleName().toString());
            } else if (a.getAnnotation(Header.class) == null//
                    && a.getAnnotation(Cookies.class) == null//
                    && a.getAnnotation(Extra.class) == null//
                    && a.getAnnotation(Body.class) == null) {
                set.add(a.getSimpleName().toString());
            } else {
                continue;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            sb.append(s).append(',');
        }
        int l = sb.length();
        orderKey = (l == 0) ? sb.toString() : sb.substring(0, l - 1);
        return orderKey;
    }

}

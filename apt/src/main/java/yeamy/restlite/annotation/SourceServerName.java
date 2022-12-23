package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.TreeSet;

class SourceServerName {
    public final String resource, params, ifHas;

    public SourceServerName(String resource, List<? extends VariableElement> arguments) {
        this.resource = resource;
        TreeSet<String> params = new TreeSet<>();
        for (VariableElement a : arguments) {
            Param pa = a.getAnnotation(Param.class);
            if (pa != null && pa.required()) {
                params.add(a.getSimpleName().toString());
            } else if (a.getAnnotation(Header.class) == null//
                    && a.getAnnotation(Cookies.class) == null//
                    && a.getAnnotation(Body.class) == null//
                    && ProcessEnvironment.getBody(a) == null) {
                params.add(a.getSimpleName().toString());
            } else {
                continue;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String p : params) {
            sb.append(p).append(',');
        }
        int l = sb.length();
        this.params = (l == 0) ? "" : sb.substring(0, l - 1);
        sb.delete(0, sb.length());
        if (params.size() > 0) {
            sb.append("if (");
            boolean first = true;
            for (String param : params) {
                if (first) {
                    first = false;
                } else {
                    sb.append("&&");
                }
                sb.append("r.has(\"").append(param).append("\")");
            }
        }
        this.ifHas = sb.toString();
    }

    public boolean isNoParam() {
        return params.length() == 0;
    }

    public String getName(String httpMethod) {
        return resource + ':' + httpMethod + ':' + params;
    }

}

package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

class SourceServerName {
    public final String resource, orderKey;
    private final ArrayList<String> httpMethods = new ArrayList<>();
    private final TreeSet<String> params = new TreeSet<>();

    public SourceServerName(String resource, List<? extends VariableElement> arguments) {
        this.resource = resource;
        TreeSet<String> set = this.params;
        for (VariableElement a : arguments) {
            Param pa = a.getAnnotation(Param.class);
            if (pa != null && pa.required()) {
                set.add(a.getSimpleName().toString());
            } else if (a.getAnnotation(Header.class) == null//
                    && a.getAnnotation(Cookies.class) == null//
                    && a.getAnnotation(Body.class) == null//
                    && ProcessEnvironment.getBody(a) == null) {
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
        this.orderKey = (l == 0) ? "" : sb.substring(0, l - 1);
    }

    public void addHttpMethod(String httpMethod) {
        httpMethods.add(httpMethod);
    }

    public TreeSet<String> getParams() {
        return params;
    }
}

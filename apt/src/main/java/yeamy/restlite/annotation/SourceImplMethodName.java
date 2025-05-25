package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

class SourceImplMethodName {
    public final String resource, params, ifHas;

    public SourceImplMethodName(String resource, List<? extends VariableElement> arguments) {
        this.resource = resource;
        TreeSet<String> params = new TreeSet<>();
        for (VariableElement a : arguments) {
            if (hasParam(a)) {
                params.add(a.getSimpleName().toString());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String p : params) {
            sb.append(p).append(',');
        }
        int l = sb.length();
        this.params = (l == 0) ? "" : sb.substring(0, l - 1);
        sb.delete(0, sb.length());
        if (!params.isEmpty()) {
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

    private static boolean hasParam(VariableElement e) {
        Param pa = e.getAnnotation(Param.class);
        if (pa != null) return pa.required();
        if (e.getAnnotation(Header.class) != null) return false;
        if (e.getAnnotation(Cookies.class) != null) return false;
        if (e.getAnnotation(Attribute.class) != null) return false;
        if (e.getAnnotation(Parts.class) != null) return false;
        if (e.getAnnotation(Body.class) != null) return false;
        if (e.getAnnotation(Inject.class) != null) return false;
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            Element annType = am.getAnnotationType().asElement();
            if (annType.getAnnotation(BodyFactory.class) != null) return false;
            if (annType.getAnnotation(PartFactory.class) != null) return false;
            if (annType.getAnnotation(CookieFactory.class) != null) return false;
            ParamFactory ann = annType.getAnnotation(ParamFactory.class);
            if (ann == null) continue;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(ann.requiredMethod())) {
                    Object v = entry.getValue().getValue();
                    return (v instanceof Boolean b) ? b : Boolean.parseBoolean(v.toString());
                }
            }
        }
        return true;
    }
}

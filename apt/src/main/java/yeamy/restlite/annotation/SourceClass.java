package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;

/**
 * @see SourceServlet
 * @see SourceWebFilter
 */
abstract class SourceClass {
    protected String pkg;
    protected HashMap<String, String> imports = new HashMap<>();

    public String getPackage() {
        return pkg;
    }

    public String imports(String clz) {
        String sn = clz.substring(clz.lastIndexOf('.') + 1);
        String clz2 = imports.get(sn);
        if (clz2 == null) {
            imports.put(sn, clz);
            return sn;
        }
        if (clz2.equals(clz)) {
            return sn;
        }
        return clz;
    }

    public String imports(TypeElement t) {
        String sn = t.getSimpleName().toString();
        String clz2 = imports.get(sn);
        String clz = t.toString();
        if (clz2 == null) {
            imports.put(sn, clz);
            return sn;
        }
        if (clz2.equals(clz)) {
            return sn;
        }
        return clz;
    }

    public abstract void create() throws IOException;

    public static CharSequence convStr(CharSequence str) {
        StringBuilder sb = new StringBuilder(str);
        TextUtils.replace(sb, "\\", "\\\\");
        TextUtils.replace(sb, "\"", "\\\"");
        return sb;
    }
}

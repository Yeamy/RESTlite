package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
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

    protected static void createSourceFile(ProcessingEnvironment env, String file, CharSequence txt) throws IOException {
        JavaFileObject f = env.getFiler().createSourceFile(file);
        try (OutputStream os = f.openOutputStream()) {
            os.write(txt.toString().getBytes());
            os.flush();
        }
    }

    public static CharSequence convStr(CharSequence str) {
        StringBuilder sb = new StringBuilder(str);
        TextUtils.replace(sb, "\\", "\\\\");
        TextUtils.replace(sb, "\"", "\\\"");
        return sb;
    }
}

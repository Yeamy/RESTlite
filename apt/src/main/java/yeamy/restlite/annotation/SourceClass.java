package yeamy.restlite.annotation;

import yeamy.restlite.utils.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * @see SourceServlet
 * @see SourceWebFilter
 */
abstract class SourceClass {
    public final String pkg;
    private final HashMap<String, String> imports = new HashMap<>();

    public SourceClass(String pkg) {
        this.pkg = pkg;
    }

    private static final String[] BASE_TYPE = {
            "boolean", "byte", "char", "short", "int", "long", "float", "double",
            "boolean[]", "byte[]", "char[]", "short[]", "int[]", "long[]", "float[]", "double[]"
    };

    public String imports(String clz) {
        if (clz.lastIndexOf('.') == -1 && TextUtils.in(clz, BASE_TYPE)) {
            return clz;
        }
        if (clz.startsWith("java.lang.") && clz.indexOf('.', 10) == -1) {
            return clz.substring(10);
        }
        String ar = "";
        if (clz.charAt(clz.length() - 1) == ']') {
            for (int i = clz.length() - 2; i > 0; i--) {
                char c = clz.charAt(i);
                if (c != '[' && c != ']') {
                    ar = clz.substring(i);
                    break;
                }
            }
        }
        String vp = "";
        int i = clz.indexOf('<');
        if (i > 0) {
            StringBuilder sb = new StringBuilder("<");
            for (String st : clz.substring(i + 1, clz.lastIndexOf('>')).split(",")) {
                String clz2 = st.trim();
                if (clz2.equals("?")) {
                    sb.append("?,");
                } else {
                    sb.append(imports(clz2)).append(',');
                }
            }
            sb.setCharAt(sb.length() - 1, '>');
            vp = sb.toString();
            clz = clz.substring(0, i);
        }
        int beginIndex = clz.lastIndexOf('.');
        String sn = clz.substring(beginIndex + 1);
        if (clz.substring(beginIndex).equals(pkg)) {
            return sn + vp + ar;
        }
        String clz2 = imports.get(sn);
        if (clz2 == null) {
            imports.put(sn, clz);
            return sn + vp + ar;
        }
        if (clz2.equals(clz)) {
            return sn + vp + ar;
        }
        return clz + vp + ar;
    }

    public String imports(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return tm.toString();
        }
        return imports(tm.toString());
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

    protected void createSourceFile(ProcessingEnvironment env, String file, CharSequence content) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (pkg.length() > 0) {
            sb.append("package ").append(pkg).append(';');
        }
        for (String clz : new TreeSet<>(imports.values())) {
            sb.append("import ").append(clz).append(';');
        }
        sb.append(content);
        JavaFileObject f = env.getFiler().createSourceFile(file);
        try (OutputStream os = f.openOutputStream()) {
            os.write(sb.toString().getBytes());
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
